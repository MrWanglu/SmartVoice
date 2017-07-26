package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.*;
import cn.fintecher.pangolin.dataimp.model.UpLoadFileModel;
import cn.fintecher.pangolin.dataimp.repository.DataImportRecordRepository;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelFileRepository;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelRepository;
import cn.fintecher.pangolin.dataimp.repository.TemplateDataModelRepository;
import cn.fintecher.pangolin.dataimp.util.ExcelUtil;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 9:35 2017/7/20
 */
@Service("dataInfoExcelService")
public class DataInfoExcelService {

    @Autowired
    DataInfoExcelRepository dataInfoExcelRepository;

    @Autowired
    MongoSequenceService mongoSequenceService;

    @Autowired
    DataImportRecordRepository dataImportRecordRepository;

    @Autowired
    TemplateDataModelRepository templateDataModelRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private DataInfoExcelFileRepository dataInfoExcelFileRepository;

    private  final Logger logger=LoggerFactory.getLogger(DataInfoExcelService.class);
    /**
     * Excel数据导入
     * @param dataImportRecord
     * @param user
     * @throws Exception
     */
    public List<CellError> importExcelData(DataImportRecord dataImportRecord,User user) throws Exception{
        //获取上传的文件
        ResponseEntity<UploadFile> fileResponseEntity=null;
        try {
            fileResponseEntity= restTemplate.getForEntity(Constants.FILEID_SERVICE_URL.concat("getUploadFile/").concat(dataImportRecord.getFileId()), UploadFile.class);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception("获取上传文件失败");
        }
        UploadFile file=fileResponseEntity.getBody();
        if(Objects.isNull(file)){
            throw new Exception("获取Excel数据失败");
        }
        //判断文件类型是否为Excel
        if(!Constants.EXCEL_TYPE_XLS.equals(file.getType()) && !Constants.EXCEL_TYPE_XLSX.equals(file.getType())){
            throw new Exception("数据文件为非Excel数据");
        }
        //获取模板数据
        TemplateDataModel templateDataModel = null;
        int[] startRow = new int[]{0};
        int[] startCol =new int[]{0};
        //通过模板配置解析Excel数据
        if(StringUtils.isNotBlank(dataImportRecord.getTemplateId())){
            templateDataModel=templateDataModelRepository.findOne(dataImportRecord.getTemplateId());
            if(Objects.nonNull(templateDataModel)){
                startRow = new int[]{Integer.parseInt(templateDataModel.getDataRowNum())};
                startCol = new int[]{Integer.parseInt(templateDataModel.getDataColNum())};
            }else{
                throw new Exception("导入模板配置信息缺失");
            }
        }
        Class<?>[] dataClass = {DataInfoExcel.class};
        ExcelSheetObj excelSheetObj= ExcelUtil.parseExcelSingle(file,dataClass,startRow,startCol,templateDataModel.getTemplateExcelInfoList());
        List<CellError> cellErrorList =null;
        if(Objects.nonNull(excelSheetObj)){
            cellErrorList=excelSheetObj.getCellErrorList();
            List dataList=excelSheetObj.getDatasList();
            //验证数据的合法性
            validityDataInfoExcel(cellErrorList,dataList);
            if(cellErrorList.isEmpty()){
                //导入数据记录
                dataImportRecord.setOperator(user.getId());
                dataImportRecord.setOperatorName(user.getRealName());
                dataImportRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
                dataImportRecord.setCompanyCode(user.getCompanyCode());
                //批次号
                String batchNumber=mongoSequenceService.getNextSeq(Constants.ORDER_SEQ,user.getCompanyCode());
                dataImportRecord.setBatchNumber(batchNumber);
                dataImportRecordRepository.save(dataImportRecord);
                //开始保存数据
                for (Object obj : dataList) {
                    DataInfoExcel tempObj = (DataInfoExcel) obj;
                    tempObj.setBatchNumber(batchNumber);
                    tempObj.setDataSources(Constants.DATA_SOURCE);
                    tempObj.setPrinCode(dataImportRecord.getPrincipalId());
                    tempObj.setPrinName(dataImportRecord.getPrincipalName());
                    tempObj.setOperator(user.getId());
                    tempObj.setOperatorName(user.getRealName());
                    tempObj.setOperatorTime(ZWDateUtil.getNowDateTime());
                    tempObj.setCompanyCode(user.getCompanyCode());
                    tempObj.setDepartId(user.getDepartment().getId());
                    tempObj.setDepartName(user.getDepartment().getName());
                    tempObj.setCaseHandNum(dataImportRecord.getHandNumber());
                    tempObj.setPaymentStatus("M".concat(String.valueOf(tempObj.getOverDuePeriods() == null ? "M0" : tempObj.getOverDuePeriods())));
                    tempObj.setDelegationDate(dataImportRecord.getDelegationDate());
                    tempObj.setCloseDate(dataImportRecord.getCloseDate());
                    String caseNumber=mongoSequenceService.getNextSeq(Constants.CASE_SEQ,user.getCompanyCode());
                    tempObj.setCaseNumber(caseNumber);
                    dataInfoExcelRepository.save(tempObj);
                }
            }
        }
        return cellErrorList;
    }

    /**
     * 验证必要数据合法性
     */
    private void validityDataInfoExcel(List<CellError> cellErrorList,List datas) {
        if(Objects.isNull(cellErrorList)){
            cellErrorList=new ArrayList<>();
        }
        for(int i=0;i<datas.size();i++ ){
            DataInfoExcel tempObj = (DataInfoExcel) datas.get(i);
            if (StringUtils.isBlank(tempObj.getPersonalName())) {
                CellError cellError = new CellError();
                cellError.setErrorMsg("第["+i+"]行的客户姓名为空");
                cellErrorList.add(cellError);
            }
            if (StringUtils.isBlank(tempObj.getProductName())) {
                CellError cellError = new CellError();
                cellError.setErrorMsg("客户[".concat(tempObj.getPersonalName()).concat("]的产品名称为空"));
                cellErrorList.add(cellError);
            }
            if (StringUtils.isBlank(tempObj.getIdCard())) {
                CellError cellError = new CellError();
                cellError.setErrorMsg("客户[".concat(tempObj.getPersonalName()).concat("]的身份证号为空"));
                cellErrorList.add(cellError);
            } else {
                if (!IdcardUtils.validateCard(tempObj.getIdCard())) {
                    CellError cellError = new CellError();
                    cellError.setErrorMsg("客户[".concat(tempObj.getPersonalName()).concat("]的身份证号[").concat(tempObj.getIdCard()).concat("]不合法"));
                    cellErrorList.add(cellError);
                }
            }
        }
    }

    /**
     * 获取导入数据的批次号
     * @param user
     * @return
     */
    public List<String> queryBatchNumGroup(User user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("creator").is(user.getUserName()));
        List<String> batchNumList = mongoTemplate.getCollection("dataInfoExcel")
                .distinct("batchNumber", query.getQueryObject());
        return batchNumList;
    }

    /**
     * 上传附件
     * @param upLoadFileModel
     * @param user
     */
    public void uploadCaseFileSingle(UpLoadFileModel upLoadFileModel, User user){
        //删除原有的附件信息
        DataInfoExcelFile obj=new DataInfoExcelFile();
        obj.setCaseNumber(upLoadFileModel.getCaseNum());
        obj.setBatchNumber(upLoadFileModel.getBatchNumber());
        obj.setOperator(user.getId());
        obj.setCompanyCode(user.getCompanyCode());
        dataInfoExcelFileRepository.delete(obj);
        List<String> fileIdList = upLoadFileModel.getFileIdList();
        if(!fileIdList.isEmpty()){
            StringBuilder sb=new StringBuilder();
            for (String id : fileIdList) {
                sb.append(id).append(",");
            }
            String ids=fileIdList.subList(0,fileIdList.lastIndexOf(",")).toString();
            ParameterizedTypeReference<List<UploadFile>> responseType = new ParameterizedTypeReference<List<UploadFile>>(){};
            ResponseEntity<List<UploadFile>> resp = restTemplate.exchange(Constants.FILEID_SERVICE_URL.concat("getAllUploadFileByIds/").concat(ids),
                    HttpMethod.GET, null, responseType);
            List<UploadFile> uploadFileList=resp.getBody();
            List<DataInfoExcelFile> dataInfoExcelFiles=new ArrayList<>();
            for(UploadFile uploadFile:uploadFileList){
                DataInfoExcelFile dataInfoExcelFile=new DataInfoExcelFile();
                dataInfoExcelFile.setCompanyCode(user.getCompanyCode());
                dataInfoExcelFile.setCaseNumber(upLoadFileModel.getCaseNum());
                dataInfoExcelFile.setCaseId(upLoadFileModel.getCaseId());
                dataInfoExcelFile.setFileId(uploadFile.getId());
                dataInfoExcelFile.setFileName(uploadFile.getRealName());
                dataInfoExcelFile.setFileUrl(uploadFile.getUrl());
                dataInfoExcelFile.setBatchNumber(upLoadFileModel.getBatchNumber());
                dataInfoExcelFile.setOperator(user.getId());
                dataInfoExcelFile.setOperatorName(user.getRealName());
                dataInfoExcelFile.setOperatorTime(ZWDateUtil.getNowDateTime());
                dataInfoExcelFiles.add(dataInfoExcelFile);
            }
            dataInfoExcelFileRepository.save(dataInfoExcelFiles);
        }
    }

    /**
     * 按批次号删除案件信息
     * @param batchNumber
     * @param user
     * @throws Exception
     */
    public void deleteCasesByBatchNum(String batchNumber, User user)  {
        //删除案件信息
        DataInfoExcel dataInfoExcel = new DataInfoExcel();
        dataInfoExcel.setBatchNumber(batchNumber);
        dataInfoExcel.setCompanyCode(user.getCompanyCode());
        dataInfoExcelRepository.delete(dataInfoExcel);
        //删除附件信息
        DataInfoExcelFile dataInfoExcelFile=new DataInfoExcelFile();
        dataInfoExcelFile.setCompanyCode(user.getCompanyCode());
        dataInfoExcelFile.setBatchNumber(batchNumber);
        dataInfoExcelFileRepository.delete(dataInfoExcelFile);
    }

}
