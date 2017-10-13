package cn.fintecher.pangolin.business.scheduled;

import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by sun on 2017/9/27.
 */
@Configuration
@EnableScheduling
public class CaseFollowRecordScheduled {

    private static final Logger log = LoggerFactory.getLogger(CaseFollowRecordScheduled.class);

    @Autowired
    private CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 59 23 * * ?")
    private void caseAutoRecoverTask() {
        log.debug("跟进记录录音更新任务调度开始...");
        try {
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            Iterable<CaseFollowupRecord> all = caseFollowupRecordRepository.findAll(qCaseFollowupRecord.callType.eq(CaseFollowupRecord.CallType.BPYUYIN.getValue()).and(qCaseFollowupRecord.collectionWay.eq(CaseFollowupRecord.CollectionWayEnum.AUTO.getValue())).and(qCaseFollowupRecord.fileName.isNotNull()).and(qCaseFollowupRecord.fileName.isNotEmpty()));
            Iterator<CaseFollowupRecord> iterator = all.iterator();
            while (iterator.hasNext()) {
                CaseFollowupRecord caseFollowupRecord = iterator.next();
                SysParam param = restTemplate.getForEntity("http://business-service/api/sysParamResource?companyCode=" + caseFollowupRecord.getCompanyCode() + "&code=" + Constants.PHONE_BF_URL + "&type=" + Constants.PHONE_BF_TYPE, SysParam.class).getBody();
                RestTemplate template = new RestTemplate();
                ResponseEntity entity = template.getForEntity("http://"+param.getValue()+"/getfilelength?filepath="+caseFollowupRecord.getFilePath()+"&filename="+caseFollowupRecord.getFileName(),String.class);
                String fileName = caseFollowupRecord.getFileName();
//                String[] fileNames = fileName.split("=");
//                String startTime = fileNames[1];
                String startTime = fileName.substring(0,15).replaceAll("-","");//去"-"
                startTime = startTime.substring(0,4)+"-"+startTime.substring(4,6)+"-"+startTime.substring(6,8)+" "+startTime.substring(8,10)+":"+startTime.substring(10,12)+":"+startTime.substring(12,14);
                Date startDate = ZWDateUtil.getFormatDateTime(startTime);
                caseFollowupRecord.setOpUrl("http://"+param.getValue()+"/getfile?filepath="+caseFollowupRecord.getFilePath()+"&filename="+caseFollowupRecord.getFileName());
                caseFollowupRecord.setStartTime(startDate);//录音开始时间
                if (entity.hasBody()){
                    String fileStr = entity.getBody().toString();
                    JSONObject json = JSONObject.parseObject(fileStr);
                    int talktime = Integer.parseInt(json.get("talktime").toString());
                    caseFollowupRecord.setConnSecs(talktime);//通话时长(s)
                    long endTime = startDate.getTime()+1000*talktime;
                    Date endDate= new Date(endTime);
                    caseFollowupRecord.setEndTime(endDate);//录音结束时间
                }
                caseFollowupRecordRepository.save(caseFollowupRecord);
            }
        } catch (Exception e) {
            log.error("跟进记录录音更新任务调度错误");
            log.error(e.getMessage(), e);
        }
        log.debug("跟进记录录音更新任务调度结束");
    }
}
