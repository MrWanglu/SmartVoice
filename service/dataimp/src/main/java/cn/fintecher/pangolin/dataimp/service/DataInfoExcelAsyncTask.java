package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.DataImportRecord;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import cn.fintecher.pangolin.dataimp.model.ColumnError;
import cn.fintecher.pangolin.dataimp.repository.DataImportRecordRepository;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelRepository;
import cn.fintecher.pangolin.dataimp.repository.RowErrorRepository;
import cn.fintecher.pangolin.dataimp.repository.TemplateDataModelRepository;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sunyanping on 2017/9/25.
 */
@Component
public class DataInfoExcelAsyncTask {

    private final Logger logger = LoggerFactory.getLogger(DataInfoExcelAsyncTask.class);

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
    RabbitTemplate rabbitTemplate;

    @Autowired
    private RowErrorRepository rowErrorRepository;

    @Async
    public void saveDataExcelInfo(DataInfoExcel dataInfoExcel, List<RowError> rowErrors, DataImportRecord dataImportRecord,
                                  int i, CopyOnWriteArrayList<Integer> dataIndex) throws Exception {
        if (!dataIndex.contains(i)) {
            logger.debug("线程{}正在处理第{}数据", Thread.currentThread(), i + 1);
            dataInfoExcel.setBatchNumber(dataImportRecord.getBatchNumber());
            dataInfoExcel.setDataSources(Constants.DataSource.IMPORT.getValue());
            dataInfoExcel.setPrinCode(dataImportRecord.getPrincipalId());
            dataInfoExcel.setPrinName(dataImportRecord.getPrincipalName());
            dataInfoExcel.setOperator(dataImportRecord.getOperator());
            dataInfoExcel.setOperatorName(dataImportRecord.getOperatorName());
            dataInfoExcel.setOperatorTime(ZWDateUtil.getNowDateTime());
            dataInfoExcel.setCompanyCode(dataImportRecord.getCompanyCode());
            dataInfoExcel.setPaymentStatus("M".concat(String.valueOf(dataInfoExcel.getOverDuePeriods() == null ? "M0" : dataInfoExcel.getOverDuePeriods())));
            dataInfoExcel.setDelegationDate(dataImportRecord.getDelegationDate());
            dataInfoExcel.setCloseDate(dataImportRecord.getCloseDate());
            String caseNumber = mongoSequenceService.getNextSeq(Constants.CASE_SEQ, dataImportRecord.getCompanyCode(), Constants.CASE_SEQ_LENGTH).concat(dataImportRecord.getCompanySequence());
            dataInfoExcel.setCaseNumber(caseNumber);
            if (!rowErrors.isEmpty()) {
                for (RowError rowError : rowErrors) {
                if (rowError.getRowIndex() == i+1) {
                    rowError.setName(dataInfoExcel.getPersonalName());
                    rowError.setIdCard(dataInfoExcel.getIdCard());
                    rowError.setPhone(dataInfoExcel.getMobileNo());
                    rowError.setBatchNumber(dataImportRecord.getBatchNumber());
                    rowError.setCaseNumber(caseNumber);
                    rowErrorRepository.save(rowError);

                    List<ColumnError> columnErrorList = rowError.getColumnErrorList();
                    for (ColumnError columnError : columnErrorList) {
                        if (dataInfoExcel.getColor() == 0 && columnError.getErrorLevel() == ColumnError.ErrorLevel.PROMPT.getValue()) {
                            dataInfoExcel.setColor(2);
                        }
                        if (dataInfoExcel.getColor() == 0 && columnError.getErrorLevel() == ColumnError.ErrorLevel.FORCE.getValue()) {
                            dataInfoExcel.setColor(1);
                            break;
                        }
                        if (dataInfoExcel.getColor() == 2 && columnError.getErrorLevel() == ColumnError.ErrorLevel.FORCE.getValue()) {
                            dataInfoExcel.setColor(1);
                            break;
                        }
                    }
                    break;
                }
                }
            }
            dataInfoExcelRepository.save(dataInfoExcel);
            logger.debug("第{}条数据处理完成", i + 1);
            dataIndex.add(i);
        }
    }
}
