package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.*;
import cn.fintecher.pangolin.dataimp.model.DataInfoExcelFileExist;
import cn.fintecher.pangolin.dataimp.model.ImportResultModel;
import cn.fintecher.pangolin.dataimp.model.UpLoadFileModel;
import cn.fintecher.pangolin.dataimp.repository.*;
import cn.fintecher.pangolin.entity.CaseInfoFile;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.DataInfoExcelModel;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.ConfirmDataInfoMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.ExcelExportUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
    ParseExcelService parseExcelService;

    @Autowired
    RowErrorRepository rowErrorRepository;

    private final Logger logger = LoggerFactory.getLogger(DataInfoExcelService.class);

    /**
     * 导入数据存放
     */
    private CopyOnWriteArrayList<DataInfoExcel> dataInfoExcelList = new CopyOnWriteArrayList();

    /**
     * 严重错误信息存放
     */
    private CopyOnWriteArrayList<RowError> forceErrorList = new CopyOnWriteArrayList();

    /**
     * 提醒错误信息存放
     */
    private CopyOnWriteArrayList<RowError> promptErrorList = new CopyOnWriteArrayList();

    /**
     * Excel数据导入
     *
     * @param dataImportRecord
     * @param user
     * @throws Exception
     */
    public ImportResultModel importExcelData(DataImportRecord dataImportRecord, User user) throws Exception {
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
        Sheet excelSheet = null;
        try {
            excelSheet = parseExcelService.getExcelSheets(file);
            if (Objects.isNull(excelSheet)) {
                throw new RuntimeException("获取Excel对象错误");
            }
        } catch (Exception e) {
            throw new RuntimeException("获取Excel对象错误");
        }
        int startRow = 0;
        int startCol = 0;
        //获取模板数据
        TemplateDataModel templateDataModel = null;
        //通过模板配置解析Excel数据
        List<TemplateExcelInfo> templateExcelInfoList = null;
        if (StringUtils.isNotBlank(dataImportRecord.getTemplateId())) {
            templateDataModel = templateDataModelRepository.findOne(dataImportRecord.getTemplateId());
            if (Objects.nonNull(templateDataModel)) {
                startRow = Integer.parseInt(templateDataModel.getDataRowNum());
                startCol = Integer.parseInt(templateDataModel.getDataColNum());
                templateExcelInfoList = templateDataModel.getTemplateExcelInfoList();
            } else {
                throw new Exception("导入模板配置信息缺失");
            }
        }
        ResponseEntity<Company> entity = null;
        try {
            entity = restTemplate.getForEntity(Constants.COMPANY_URL.concat(user.getCompanyCode()), Company.class);
            if (!entity.hasBody()) {
                throw new Exception("获取公司序列号失败!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("获取公司序列号失败");
        }
        if (Objects.isNull(templateExcelInfoList)) {
            try {
                boolean b = parseExcelService.checkHeader(excelSheet, 0, 0);
                if (!b) {
                    throw new Exception("请使用系统默认模板或者配置导入模板");
                }
            } catch (Exception e) {
                throw new RuntimeException("导入失败!");
            }
        }
        try {
            Company company = entity.getBody();
            //批次号
            String batchNumber = mongoSequenceService.getNextSeq(Constants.ORDER_SEQ, user.getCompanyCode(), Constants.ORDER_SEQ_LENGTH);
            batchNumber = company.getSequence().concat(batchNumber);
            dataImportRecord.setBatchNumber(batchNumber);
            dataImportRecord.setOperator(user.getId());
            dataImportRecord.setOperatorName(user.getRealName());
            dataImportRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
            dataImportRecord.setCompanyCode(user.getCompanyCode());
            dataImportRecord.setCompanySequence(company.getSequence());
            dataImportRecordRepository.save(dataImportRecord);
            //开始保存数据
            dataInfoExcelList.clear();
            forceErrorList.clear();
            promptErrorList.clear();
            parseExcelService.parseSheet(excelSheet, startRow, startCol, DataInfoExcel.class, dataImportRecord, templateExcelInfoList, dataInfoExcelList, forceErrorList, promptErrorList);
            StopWatch watch = new StopWatch();
            watch.start();
            if (forceErrorList.isEmpty()) {
                dataInfoExcelRepository.save(dataInfoExcelList);
                rowErrorRepository.save(promptErrorList);
            }
            watch.stop();
            logger.debug("保存数据共耗时{}ms", watch.getTotalTimeMillis());
            logger.debug("数据处理保存完成");
            ImportResultModel model = new ImportResultModel();
            model.setBatchNumber(batchNumber);
            Collections.sort(forceErrorList, Comparator.comparingInt(RowError::getRowIndex));
            model.setRowErrorList(forceErrorList);
            return model;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("导入失败!");
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
    public List<DataInfoExcelFileExist> checkCasesFile(User user, String batchNumber) {
        List<DataInfoExcelFileExist> dataInfoExcelFileExistList = new ArrayList<>();
        QDataInfoExcel qDataInfoExcel = QDataInfoExcel.dataInfoExcel;
        Iterable<DataInfoExcel> dataInfoExcelIterable = dataInfoExcelRepository.findAll(qDataInfoExcel.operator.eq(user.getId())
                .and(qDataInfoExcel.companyCode.eq(user.getCompanyCode())).and(qDataInfoExcel.batchNumber.eq(batchNumber)));
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
        QDataInfoExcel qDataInfoExcel = QDataInfoExcel.dataInfoExcel;
        Iterable<DataInfoExcel> dataInfoExcelIterable = dataInfoExcelRepository.findAll(qDataInfoExcel.operator.eq(user.getId())
                .and(qDataInfoExcel.companyCode.eq(user.getCompanyCode()))
                .and(qDataInfoExcel.batchNumber.eq(batchNumber)));
        List<DataInfoExcelModel> dataInfoExcelModelList = new ArrayList<>();
        List<DataInfoExcelHis> dataInfoExcelHisList = new ArrayList<>();
        int dataTotal = 0;
        for (Iterator iterator = dataInfoExcelIterable.iterator(); iterator.hasNext(); ) {
            dataTotal = dataTotal + 1;
            DataInfoExcel dataInfoExcel = (DataInfoExcel) iterator.next();
            //包含严重错误的一批案件不允许确认
            if (Objects.equals(dataInfoExcel.getColor(), DataInfoExcel.Color.RED.getValue())) {
                throw new RuntimeException("此批案件存在严重错误不允许确认");
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
        Iterable<RowError> rowErrors = rowErrorRepository.findAll(QRowError.rowError.batchNumber.eq(batchNumber));
        ConfirmDataInfoMessage msg = new ConfirmDataInfoMessage();
        msg.setDataInfoExcelModelList(dataInfoExcelModelList);
        msg.setDataCount(dataInfoExcelModelList.size());
        msg.setUser(user);
        //发送消息
        rabbitTemplate.convertAndSend(Constants.DATAINFO_CONFIRM_QE, msg);
        //移动导入的数据
        dataInfoExcelRepository.delete(dataInfoExcelIterable);
        dataInfoExcelHisRepository.save(dataInfoExcelHisList);
        rowErrorRepository.delete(rowErrors);
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

    public String exportError(String batchNumber, String companyCode, List<RowError> forceErrorList) {
        List<RowError> dataList = null;
        if (batchNumber != null) {
            QRowError qRowError = QRowError.rowError;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qRowError.batchNumber.eq(batchNumber));
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(qRowError.companyCode.eq(companyCode));
            }
            Iterable<RowError> all = rowErrorRepository.findAll(builder);
            dataList = IterableUtils.toList(all);
        } else if (forceErrorList != null && !forceErrorList.isEmpty()) {
            dataList = forceErrorList;
        } else {
            throw new RuntimeException("导出错误!");
        }
        Collections.sort(dataList, Comparator.comparingInt(RowError::getRowIndex));
        String[] title = {"Excel行号", "案件编号", "客户姓名", "手机号", "身份证号", "案件金额(元)", "错误内容"};
        HashMap<String, String> headMap = ExcelExportUtil.createHeadMap(title, RowError.class);

        File file = null;
        FileOutputStream fileOutputStream = null;
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(1000);
             ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            ExcelExportUtil.createExcelData(workbook, headMap, dataList, Constants.ROW_MAX - 1);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(ZWDateUtil.getFormatNowDate("yyyyMMddhhmmss") + "错误报告.xlsx");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity(Constants.UPLOAD_FILE_URL, param, String.class);
            if (url == null) {
                throw new RuntimeException("获取上传服务器文件失败!");
            } else {
                return url.getBody();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("获取上传服务器文件失败!");
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }
}
