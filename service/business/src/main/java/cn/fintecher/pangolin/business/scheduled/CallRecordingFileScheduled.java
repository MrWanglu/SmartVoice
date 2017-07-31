package cn.fintecher.pangolin.business.scheduled;


import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import org.apache.commons.collections4.IteratorUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 电话录音获取调度
 * Created by ChenChang on 2017/4/14.
 */
@Component
@EnableScheduling
@Lazy(value = false)
public class CallRecordingFileScheduled {
    private final Logger log = LoggerFactory.getLogger(CallRecordingFileScheduled.class);
    @Autowired
    private CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Autowired
    private RestTemplate restTemplate;
//    @Scheduled(cron = "1 0/10 * * * ?")
//    void checkCallRecordFile() throws IOException {
//        log.info("定时调度 录音文件是否已更新" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
//        Specification<AccFollowup> specification = (root, query, cb) -> {
//            Predicate predicate = cb.conjunction();
//            Predicate p0 = cb.equal(root.get("follType"), AccFollowup.FollType.Tel.getValue());
//            Predicate p1 = cb.and(cb.isNull(root.get("follOpurl")));
//            Predicate p2 = cb.and(cb.equal(root.get("follOpurl"), ""));
//            //超过一周的不处理了
//            DateTime dateTime = new DateTime();
//            Predicate p3 = cb.greaterThan(root.get("createTime"), dateTime.minusWeeks(1).toDate());
//            Predicate p4 = cb.equal(root.get("follLoad"), 1);
//            query.where(cb.and(p0, p4, p3, cb.or(p1, p2)));
//            return query.getRestriction();
//        };
//        List<AccFollowup> list = accFollowupService.findAll(specification);
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        for (AccFollowup accFollowup : list) {
//            try {
//                if (StringUtils.isNotBlank(accFollowup.getFollRecoderid())) {
//                    log.info("定时调度 录音文件开始更新 {} ", accFollowup.getFollRecoderid());
//                    AddTaskVoiceFileMessage addTaskVoiceFileMessage = new AddTaskVoiceFileMessage();
//                    addTaskVoiceFileMessage.setTaskid(accFollowup.getFollTaskid());
//                    addTaskVoiceFileMessage.setRecorderId(accFollowup.getFollRecoderid());
//                    addTaskVoiceFileMessage.setTaskcallerId(accFollowup.getFollTaskcallerid());
//                    ResponseEntity<String> responseEntity = null;
//                    try {
//                        HttpEntity<AddTaskVoiceFileMessage> entity1 = new HttpEntity<AddTaskVoiceFileMessage>(addTaskVoiceFileMessage, headers);
//                        responseEntity = restTemplate.exchange("http://SMA-SERVICE/api/smaResource/addTaskVoiceFileByTaskId", HttpMethod.POST, entity1, String.class);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    if (responseEntity != null && responseEntity.getStatusCode().is2xxSuccessful()) {
//                        if (responseEntity.hasBody()) {
//                            String url = responseEntity.getBody();
//                            //audio/mpeg
//                            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//                            RestTemplate restTemplate1 = new RestTemplate();
//                            ResponseEntity<byte[]> response = restTemplate1.exchange(url, HttpMethod.GET, entity, byte[].class);
//                            String filePath = FileUtils.getTempDirectoryPath().concat("record.mp3");
//                            FileOutputStream output = new FileOutputStream(new File(filePath));
//                            IOUtils.write(response.getBody(), output);
//                            IOUtils.closeQuietly(output);
//                            FileSystemResource resource = new FileSystemResource(new File(filePath));
//                            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//                            param.add("file", resource);
//                            url = restTemplate.postForObject("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
//                            log.debug("upload file path:{}", url);
//                            accFollowup.setFollOpurl(url);
//                            accFollowupService.save(accFollowup);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }
//    }

    /**
     * @Description : 中通天鸿下载录音调度
     */
//    CaseFollowupRecordRepository
    @Scheduled(cron = "1 0/1 * * * ?")
    void checkCallRecordFileZtth() throws IOException {
        log.info("定时调度 中通天鸿的录音调度" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            Iterator<CaseFollowupRecord> caseFollowupRecords = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.TIANHONG.getValue())).iterator();
            List<CaseFollowupRecord> caseFollowupRecordList = IteratorUtils.toList(caseFollowupRecords);
            if (Objects.nonNull(caseFollowupRecordList)) {
                for (CaseFollowupRecord caseFollowupRecord : caseFollowupRecordList) {
                    String callId = caseFollowupRecord.getTaskId();
                    ResponseEntity<String> result = restTemplate.getForEntity("http://common-service/api/smaResource/getRecordingByCallId?callId=" + callId, String.class);
                    if (Objects.nonNull(result.getBody()) && !Objects.equals("fail", result.getBody())) {
                        caseFollowupRecord.setCallType(null);
                        caseFollowupRecord.setOpUrl(result.getBody());
                        caseFollowupRecord.setLoadFlag(1);
                        caseFollowupRecordRepository.save(caseFollowupRecord);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
