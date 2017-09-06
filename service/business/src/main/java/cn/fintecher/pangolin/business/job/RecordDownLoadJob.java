package cn.fintecher.pangolin.business.job;

import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 录音下载批量
 * @Date 13:55 2017/8/11
 */
@Service("recordDownLoadJob")
@DisallowConcurrentExecution
public class RecordDownLoadJob implements Job {

    Logger logger = LoggerFactory.getLogger(RecordDownLoadJob.class);

    @Autowired
    SchedulerFactoryBean schedFactory;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    private CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        DateTime dateTime;
        Iterator<CaseFollowupRecord> caseFollowupRecords;
        List<CaseFollowupRecord> caseFollowupRecordList;
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYSPARAM_RECORD_STATUS));
        try {
            if (Objects.equals("0", sysParam.getValue())) {
                sysParam.setValue("1");
                sysParamRepository.save(sysParam);
                logger.info("录音下载批量.......");
                //erpv3 的录音下载
                logger.info("erpv3 的录音下载" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                dateTime = new DateTime();
                caseFollowupRecords = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.opUrl.isNull().and(qCaseFollowupRecord.operatorTime.gt(dateTime.minusWeeks(1).toDate())).and(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.ERPV3.getValue())).and(qCaseFollowupRecord.taskId.isNotNull()).and(qCaseFollowupRecord.recoderId.isNotNull()).and(qCaseFollowupRecord.taskcallerId.isNotNull())).iterator();
                caseFollowupRecordList = IteratorUtils.toList(caseFollowupRecords);
                HttpHeaders headers = new HttpHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);
                for (CaseFollowupRecord caseFollowupRecord : caseFollowupRecordList) {
                    try {
                        if (Objects.nonNull(caseFollowupRecord.getTaskcallerId())) {
                            logger.info("定时调度 录音文件开始更新 {} ", caseFollowupRecord.getTaskcallerId());
                            AddTaskVoiceFileMessage addTaskVoiceFileMessage = new AddTaskVoiceFileMessage();
                            addTaskVoiceFileMessage.setTaskid(caseFollowupRecord.getTaskId());
                            addTaskVoiceFileMessage.setRecorderId(caseFollowupRecord.getRecoderId());
                            addTaskVoiceFileMessage.setTaskcallerId(caseFollowupRecord.getTaskcallerId());
                            ResponseEntity<String> responseEntity = null;
                            try {
                                HttpEntity<AddTaskVoiceFileMessage> entity1 = new HttpEntity<AddTaskVoiceFileMessage>(addTaskVoiceFileMessage, headers);
                                responseEntity = restTemplate.exchange("http://common-service/api/smaResource/addTaskVoiceFileByTaskId", HttpMethod.POST, entity1, String.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()) {
                                if (responseEntity.hasBody()) {
                                    String url = responseEntity.getBody();
                                    //audio/mpeg
                                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                                    RestTemplate restTemplate1 = new RestTemplate();
                                    ResponseEntity<byte[]> response = restTemplate1.exchange(url, HttpMethod.GET, entity, byte[].class);
                                    String filePath = FileUtils.getTempDirectoryPath().concat("record.mp3");
                                    FileOutputStream output = new FileOutputStream(new File(filePath));
                                    IOUtils.write(response.getBody(), output);
                                    IOUtils.closeQuietly(output);
                                    FileSystemResource resource = new FileSystemResource(new File(filePath));
                                    MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                                    param.add("file", resource);
                                    url = restTemplate.postForObject("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
                                    logger.debug("upload file path:{}", url);
                                    caseFollowupRecord.setOpUrl(url);
                                    caseFollowupRecordRepository.save(caseFollowupRecord);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                //中通天鸿的录音下载
                logger.info("定时调度 中通天鸿的录音调度" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                try {
                    dateTime = new DateTime();
                    caseFollowupRecords = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.opUrl.isNull().and(qCaseFollowupRecord.operatorTime.gt(dateTime.minusWeeks(1).toDate())).and(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.TIANHONG.getValue())).and(qCaseFollowupRecord.taskId.isNotNull())).iterator();
                    caseFollowupRecordList = IteratorUtils.toList(caseFollowupRecords);
                    if (Objects.nonNull(caseFollowupRecordList)) {
                        for (CaseFollowupRecord caseFollowupRecord : caseFollowupRecordList) {
                            logger.info("定时调度 中通天鸿的录音调度 ", caseFollowupRecord.getTaskId());
                            String callId = caseFollowupRecord.getTaskId();
                            ResponseEntity<String> result = restTemplate.getForEntity("http://common-service/api/smaResource/getRecordingByCallId?callId=" + callId, String.class);
                            if (Objects.nonNull(result.getBody()) && !Objects.equals("fail", result.getBody())) {
                                caseFollowupRecord.setOpUrl(result.getBody());
                                caseFollowupRecordRepository.save(caseFollowupRecord);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            sysParam.setValue("0");
            sysParamRepository.save(sysParam);
        }
    }

    @Bean(name = "createRecordDownLoadJob")
    public List<CronTriggerFactoryBean> CreateRecordDownLoadJob() {
        List<CronTriggerFactoryBean> cronTriggerFactoryBeanList = new ArrayList<>();
        try {
            QSysParam qSysParam = QSysParam.sysParam;
            //获取公司码
            List<Company> companyList = companyRepository.findAll();
            for (Company company : companyList) {
                //判断该公司的跑批状态是否为启用状态
                SysParam status = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                        .and(qSysParam.code.eq(Constants.RECORD_DOWNLOAD_STATUS_CODE))
                        .and(qSysParam.type.eq(Constants.RECORD_DOWNLOAD_STATUS_TYPE))
                        .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
                if (Objects.equals(status.getValue(), "1")) {
                    continue;
                }
                SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                        .and(qSysParam.code.eq(Constants.SYSPARAM_RECORD))
                        .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
                if (Objects.nonNull(sysParam)) {
                    String cron = sysParam.getValue();
                    cron = "0 ".concat("0").concat("/").concat(cron).concat(" * * * ?");
                    JobDetail jobDetail = ConfigureQuartz.createJobDetail(RecordDownLoadJob.class, Constants.RECORD_JOB_GROUP,
                            Constants.RECORD_JOB_NAME.concat("_").concat(company.getCode()),
                            Constants.RECORD_JOB_DESC.concat("_").concat(company.getCode()));
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("companyCode", company.getCode());
                    jobDataMap.put("sysParamCode", Constants.SYSPARAM_RECORD_STATUS);
                    CronTriggerFactoryBean cronTriggerFactoryBean = ConfigureQuartz.createCronTrigger(Constants.RECORD_TRIGGER_GROUP,
                            Constants.RECORD_TRIGGER_NAME.concat("_").concat(company.getCode()),
                            "RecordDownLoadJobBean".concat("_").concat(company.getCode()),
                            Constants.RECORD_TRIGGER_DESC.concat("_").concat(company.getCode()), jobDetail, cron, jobDataMap);
                    cronTriggerFactoryBean.afterPropertiesSet();
                    schedFactory.getScheduler().deleteJob(jobDetail.getKey());
                    //加入调度器
                    schedFactory.getScheduler().scheduleJob(jobDetail, cronTriggerFactoryBean.getObject());
                    cronTriggerFactoryBeanList.add(cronTriggerFactoryBean);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return cronTriggerFactoryBeanList;
    }

}
