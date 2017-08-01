package cn.fintecher.pangolin.business.scheduled;


import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    //erpv3定时调度 拉取录音
    @Scheduled(cron = "1 0/10 * * * ?")
    void checkCallRecordFile() throws IOException {
        log.info("定时调度 录音文件是否已更新" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        DateTime dateTime = new DateTime();
        Iterator<CaseFollowupRecord> caseFollowupRecords = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.collectionType.eq(User.Type.TEL.getValue()).and(qCaseFollowupRecord.opUrl.isNull()).or(qCaseFollowupRecord.opUrl.eq("")).and(qCaseFollowupRecord.operatorTime.gt(dateTime.minusWeeks(1).toDate())).and(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.ERPV3.getValue()))).iterator();
        List<CaseFollowupRecord> caseFollowupRecordList = IteratorUtils.toList(caseFollowupRecords);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        for (CaseFollowupRecord caseFollowupRecord : caseFollowupRecordList) {
            try {
                if (Objects.nonNull(caseFollowupRecord.getTaskcallerId())) {
                    log.info("定时调度 录音文件开始更新 {} ", caseFollowupRecord.getTaskcallerId());
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
                    if (Objects.nonNull(responseEntity)&& responseEntity.getStatusCode().is2xxSuccessful()) {
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
                            log.debug("upload file path:{}", url);
                            caseFollowupRecord.setOpUrl(url);
                            caseFollowupRecordRepository.save(caseFollowupRecord);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @Description : 中通天鸿下载录音调度
     */
    @Scheduled(cron = "1 0/1 * * * ?")
    void checkCallRecordFileZtth() throws IOException {
        log.info("定时调度 中通天鸿的录音调度" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            Iterator<CaseFollowupRecord> caseFollowupRecords = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.TIANHONG.getValue())).iterator();
            List<CaseFollowupRecord> caseFollowupRecordList = IteratorUtils.toList(caseFollowupRecords);
            if (Objects.nonNull(caseFollowupRecordList)) {
                for (CaseFollowupRecord caseFollowupRecord : caseFollowupRecordList) {
                    log.info("定时调度 中通天鸿的录音调度 ", caseFollowupRecord.getTaskId());
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

    /**
     * @Description : 云羿呼叫中心的定时心跳
     */
    @Scheduled(cron = "0/60 * * * * ?")
    void callHeartBeat() throws IOException {
        log.info("云羿呼叫中心的定时心跳" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            Map<String, String> map = Constants.map;
            for (String value : map.values()) {
                log.info("云羿呼叫中心的定时心跳", value);
                Socket socket = new Socket("116.236.220.211", 12345);
                socket.setSoTimeout(10000000);
                BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String sendData = "<request><cmdType>heartbeat</cmdType><agentID>" + value + "</agentID></request>";
                String sendDataUtf82 = new String(sendData.getBytes("UTF-8"), "UTF-8");
                String head2 = "<<<length=" + sendDataUtf82.getBytes("UTF-8").length + ">>>";
                sendDataUtf82 = head2 + sendDataUtf82;
                System.out.println("心跳：" + sendDataUtf82);
                pw.print(sendDataUtf82);
                pw.flush();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
