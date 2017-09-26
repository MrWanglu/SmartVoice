package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.*;
import cn.fintecher.pangolin.dataimp.model.DataInfoExcelFileExist;
import cn.fintecher.pangolin.dataimp.model.UpLoadFileModel;
import cn.fintecher.pangolin.dataimp.repository.*;
import cn.fintecher.pangolin.entity.CaseInfoFile;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.DataInfoExcelModel;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ConfirmDataInfoMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.fintecher.pangolin.dataimp.entity.QDataInfoExcel.dataInfoExcel;

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

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private DataInfoExcelHisRepository dataInfoExcelHisRepository;

    @Autowired
    private DataInfoExcelAsyncTask dataInfoExcelAsyncTask;

    private final Logger logger = LoggerFactory.getLogger(DataInfoExcelService.class);

    CopyOnWriteArrayList<Integer> dataIndex = new CopyOnWriteArrayList<>();

    DataImportRecord dataImportRecord = new DataImportRecord();

    @Autowired
    private ExcelParseService excelParseService;
    /**
     * Excel数据导入
     *
     * @param dataImportRecord
     * @param user
     * @throws Exception
     */
    public void importExcelData(DataImportRecord dataImportRecord, User user) throws Exception {
        //获取上传的文件
        ResponseEntity<UploadFile> fileResponseEntity = null;
        try {
            fileResponseEntity = restTemplate.getForEntity(Constants.FILEID_SERVICE_URL.concat("uploadFile/").concat(dataImportRecord.getFileId()), UploadFile.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("获取上传文件失败");
        }
        UploadFile file = fileResponseEntity.getBody();
        if (Objects.isNull(file)) {
            throw new Exception("获取Excel数据失败");
        }
        //判断文件类型是否为Excel
        if (!Constants.EXCEL_TYPE_XLS.equals(file.getType()) && !Constants.EXCEL_TYPE_XLSX.equals(file.getType())) {
            throw new Exception("数据文件为非Excel数据");
        }
        //获取模板数据
        TemplateDataModel templateDataModel = null;
        int[] startRow = new int[]{0};
        int[] startCol = new int[]{0};
        //通过模板配置解析Excel数据
        List<TemplateExcelInfo> templateExcelInfoList = null;
        if (StringUtils.isNotBlank(dataImportRecord.getTemplateId())) {
            templateDataModel = templateDataModelRepository.findOne(dataImportRecord.getTemplateId());
            if (Objects.nonNull(templateDataModel)) {
                startRow = new int[]{Integer.parseInt(templateDataModel.getDataRowNum())};
                startCol = new int[]{Integer.parseInt(templateDataModel.getDataColNum())};
                templateExcelInfoList = templateDataModel.getTemplateExcelInfoList();
            } else {
                throw new Exception("导入模板配置信息缺失");
            }
        }
        Class<?>[] dataClass = {DataInfoExcel.class};
//        ExcelSheetObj excelSheetObj = ExcelUtil.parseExcelSingle(file, dataClass, startRow, startCol, templateExcelInfoList);
        ExcelSheetObj excelSheetObj = excelParseService.parseExcelSingle(file, dataClass, startRow, startCol, templateExcelInfoList);
        List<RowError> rowErrors = new ArrayList<>();
        if (Objects.nonNull(excelSheetObj)) {
            rowErrors = excelSheetObj.getSheetErrorList();
            List dataList = excelSheetObj.getDataList();

            ResponseEntity<Company> entity = restTemplate.getForEntity(Constants.COMPANY_URL.concat(user.getCompanyCode()), Company.class);
            if (!entity.hasBody()) {
                throw new Exception("获取公司序列号失败!");
            }
            Company company = entity.getBody();
            //批次号
            String batchNumber = mongoSequenceService.getNextSeq(Constants.ORDER_SEQ, user.getCompanyCode(), Constants.ORDER_SEQ_LENGTH);
            //导入数据记录
            dataImportRecord.setBatchNumber(batchNumber);
            dataImportRecord.setOperator(user.getId());
            dataImportRecord.setOperatorName(user.getRealName());
            dataImportRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
            dataImportRecord.setCompanyCode(user.getCompanyCode());
            dataImportRecord.setCompanySequence(company.getSequence());
            dataImportRecordRepository.save(dataImportRecord);
            this.dataImportRecord = dataImportRecord;
            //开始保存数据
            //开始保存数据
            logger.info("共{}条数据,开始校验保存...", dataList.size());
            StopWatch watch = new StopWatch();
            watch.start();
            dataIndex.clear();
            for (int i = 0; i < dataList.size(); i++) {
                dataInfoExcelAsyncTask.saveDataExcelInfo((DataInfoExcel) dataList.get(i), rowErrors, this.dataImportRecord, i, dataIndex);
            }
            while (dataIndex.size() != dataList.size()) {
                Thread thread = Thread.currentThread();
                thread.sleep(1);
            }
            watch.stop();
            logger.info("共{}条数据,校验保存完成,共耗时{}ms", dataList.size(), watch.getTotalTimeMillis());
            return;
        }
    }

    /**
     * 获取导入数据的批次号
     *
     * @param user
     * @return
     */
    public List<String> queryBatchNumGroup(User user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("operator").is(user.getId()).and("companyCode").is(user.getCompanyCode()));
        List<String> batchNumList = mongoTemplate.getCollection("dataInfoExcel")
                .distinct("batchNumber", query.getQueryObject());
        return batchNumList;
    }

    /**
     * 上传附件
     *
     * @param upLoadFileModel
     * @param user
     */
    public void uploadCaseFileSingle(UpLoadFileModel upLoadFileModel, User user) {
        //删除原有的附件信息
        DataInfoExcelFile obj = new DataInfoExcelFile();
        obj.setCaseNumber(upLoadFileModel.getCaseNum());
        obj.setBatchNumber(upLoadFileModel.getBatchNumber());
        obj.setOperator(user.getId());
        obj.setCompanyCode(user.getCompanyCode());
        Example<DataInfoExcelFile> example = Example.of(obj);
        List<DataInfoExcelFile> all = dataInfoExcelFileRepository.findAll(example);
        dataInfoExcelFileRepository.delete(all);
        List<String> fileIdList = upLoadFileModel.getFileIdList();
        if (!fileIdList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String id : fileIdList) {
                sb.append(id).append(",");
            }
            String ids = sb.toString();
            ParameterizedTypeReference<List<UploadFile>> responseType = new ParameterizedTypeReference<List<UploadFile>>() {
            };
            ResponseEntity<List<UploadFile>> resp = restTemplate.exchange(Constants.FILEID_SERVICE_URL.concat("uploadFile/getAllUploadFileByIds/").concat(ids),
                    HttpMethod.GET, null, responseType);
            List<UploadFile> uploadFileList = resp.getBody();
            List<DataInfoExcelFile> dataInfoExcelFiles = new ArrayList<>();
            for (UploadFile uploadFile : uploadFileList) {
                DataInfoExcelFile dataInfoExcelFile = new DataInfoExcelFile();
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
                dataInfoExcelFile.setFileType(uploadFile.getType());
                dataInfoExcelFiles.add(dataInfoExcelFile);
            }
            dataInfoExcelFileRepository.save(dataInfoExcelFiles);
        }
    }

    /**
     * 按批次号删除案件信息
     *
     * @param batchNumber
     * @param user
     * @throws Exception
     */
    public void deleteCasesByBatchNum(String batchNumber, User user) {
        //删除案件信息
        List<DataInfoExcel> dataInfoExcels = dataInfoExcelRepository.findByBatchNumberAndCompanyCode(batchNumber, user.getCompanyCode());
        dataInfoExcelRepository.delete(dataInfoExcels);
        //删除附件信息
        List<DataInfoExcelFile> dataInfoExcelFiles = dataInfoExcelFileRepository.findByBatchNumberAndCompanyCode(batchNumber, user.getCompanyCode());
        dataInfoExcelFileRepository.delete(dataInfoExcelFiles);
    }

    /**
     * 检查附件是否存在
     *
     * @param user
     * @return
     */
    public List<DataInfoExcelFileExist> checkCasesFile(User user) {
        List<DataInfoExcelFileExist> dataInfoExcelFileExistList = new ArrayList<>();
        QDataInfoExcel qDataInfoExcel = dataInfoExcel;
        Iterable<DataInfoExcel> dataInfoExcelIterable = dataInfoExcelRepository.findAll(qDataInfoExcel.operator.eq(user.getId())
                .and(qDataInfoExcel.companyCode.eq(user.getCompanyCode())));
        for (Iterator<DataInfoExcel> it = dataInfoExcelIterable.iterator(); it.hasNext(); ) {
            DataInfoExcel dataInfoExcel = it.next();
            QDataInfoExcelFile qDataInfoExcelFile = QDataInfoExcelFile.dataInfoExcelFile;
            Iterable<DataInfoExcelFile> dataInfoExcelFileIterable = dataInfoExcelFileRepository.findAll(qDataInfoExcelFile.caseId.eq(dataInfoExcel.getId()));
            if (Objects.isNull(dataInfoExcelFileIterable) || !(dataInfoExcelFileIterable.iterator().hasNext())) {
                DataInfoExcelFileExist obj = new DataInfoExcelFileExist();
                obj.setCaseId(dataInfoExcel.getId());
                obj.setCaseNumber(dataInfoExcel.getCaseNumber());
                obj.setBatchNumber(dataInfoExcel.getBatchNumber());
                obj.setMsg("缺少附件");
                dataInfoExcelFileExistList.add(obj);
            }
        }
        return dataInfoExcelFileExistList;
    }


    /**
     * 案件确认
     *
     * @param user
     */
    public void casesConfirmByBatchNum(User user, String batchNumber) {
        //查询该用户下所有未确认的案件
        QDataInfoExcel qDataInfoExcel = dataInfoExcel;
        Iterable<DataInfoExcel> dataInfoExcelIterable = dataInfoExcelRepository.findAll(qDataInfoExcel.operator.eq(user.getId())
                .and(qDataInfoExcel.companyCode.eq(user.getCompanyCode()))
                .and(qDataInfoExcel.batchNumber.eq(batchNumber)));
        List<DataInfoExcelModel> dataInfoExcelModelList = new ArrayList<>();
        List<DataInfoExcelHis> dataInfoExcelHisList = new ArrayList<>();
        int dataTotal = 0;
        for (Iterator iterator = dataInfoExcelIterable.iterator(); iterator.hasNext(); ) {
            dataTotal = dataTotal + 1;
            DataInfoExcel dataInfoExcel = (DataInfoExcel) iterator.next();
            if (Objects.equals(dataInfoExcel.getColor(), 1)) { //严重错误
                throw new RuntimeException("这批案件有存在严重错误的案件,不能确认");
            }
            DataInfoExcelModel dataInfoExcelModel = new DataInfoExcelModel();
            BeanUtils.copyProperties(dataInfoExcel, dataInfoExcelModel);
            //附件信息
            QDataInfoExcelFile qDataInfoExcelFile = QDataInfoExcelFile.dataInfoExcelFile;
            Iterable<DataInfoExcelFile> dataInfoExcelFileIterable = dataInfoExcelFileRepository.findAll(qDataInfoExcelFile.caseId.eq(dataInfoExcel.getId()));
            List<CaseInfoFile> caseInfoFileList = new ArrayList<>();
            List<DataInfoExcelFile> dataInfoExcelFileList = IteratorUtils.toList(dataInfoExcelFileIterable.iterator());
            for (DataInfoExcelFile file : dataInfoExcelFileList) {
                CaseInfoFile caseInfoFile = new CaseInfoFile();
                BeanUtils.copyProperties(file, caseInfoFile);
                caseInfoFileList.add(caseInfoFile);
            }
            dataInfoExcelModel.setCaseInfoFileList(caseInfoFileList);
            dataInfoExcelModelList.add(dataInfoExcelModel);
            DataInfoExcelHis dataInfoExcelHis = new DataInfoExcelHis();
            BeanUtils.copyProperties(dataInfoExcel, dataInfoExcelHis);
            dataInfoExcelHisList.add(dataInfoExcelHis);
        }
        ConfirmDataInfoMessage msg = new ConfirmDataInfoMessage();
        msg.setDataInfoExcelModelList(dataInfoExcelModelList);
        msg.setDataCount(dataInfoExcelModelList.size());
        msg.setUser(user);
        //发送消息
        rabbitTemplate.convertAndSend(Constants.DATAINFO_CONFIRM_QE, msg);
        //移动导入的数据
        dataInfoExcelRepository.delete(dataInfoExcelIterable);
        dataInfoExcelHisRepository.save(dataInfoExcelHisList);
    }

    public List<DataInfoExcel> queryDataInfoExcelListNoPage(DataInfoExcel dataInfoExcel, User user) throws Exception {
        try {
            Query query = new Query();
            if (StringUtils.isNotBlank(dataInfoExcel.getOperator())) {
                query.addCriteria(Criteria.where("operator").is(dataInfoExcel.getOperator()));
            }
            if (StringUtils.isNotBlank(dataInfoExcel.getBatchNumber())) {
                query.addCriteria(Criteria.where("batchNumber").is(dataInfoExcel.getBatchNumber()));
            }
            if (StringUtils.isNotBlank(dataInfoExcel.getPersonalName())) {
                query.addCriteria(Criteria.where("personalName").is(dataInfoExcel.getPersonalName()));
            }
            if (StringUtils.isNotBlank(dataInfoExcel.getIdCard())) {
                query.addCriteria(Criteria.where("idCard").is(dataInfoExcel.getIdCard()));
            }
            if (StringUtils.isNotBlank(dataInfoExcel.getProductName())) {
                query.addCriteria(Criteria.where("productName").is(dataInfoExcel.getProductName()));
            }
            return mongoTemplate.find(query, DataInfoExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
