package cn.fintecher.pangolin.business.scheduled;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoReturnRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.business.service.ReminderService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by sun on 2017/9/27.
 */
@Configuration
@EnableScheduling
public class CaseAutoRecoverScheduled {

    private static final Logger log = LoggerFactory.getLogger(CaseAutoRecoverScheduled.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;
    @Inject
    private OutsourcePoolRepository outsourcePoolRepository;
    @Inject
    private ReminderService reminderService;

    @Scheduled(cron = "0 59 23 * * ?")
    private void caseAutoRecoverTask() {
        try {
            log.debug("案件自动回收任务调度开始...");
            // 内催自动回收
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            Iterable<CaseInfo> all = caseInfoRepository.findAll(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue())
                    .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())//除过已结案
                            .and(qCaseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()))//未回收的
                            .and(qCaseInfo.recoverWay.eq(CaseInfo.RecoverWay.AUTO.getValue()))//需要自动回收的
                            .and(qCaseInfo.closeDate.before(new Date()))));//到期日期
            Iterator<CaseInfo> iterator = all.iterator();
            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseInfoReturn> caseInfoReturnList = new ArrayList<>();
            while (iterator.hasNext()) {
                CaseInfo caseInfo = iterator.next();
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());

                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setReason("案件到期自动回收");
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturn.setSource(CaseInfoReturn.Source.INTERNALCOLLECTION.getValue()); // 回收来源-内催
                caseInfoReturn.setCaseId(caseInfo);
                caseInfoReturn.setCompanyCode(caseInfo.getCompanyCode());
                caseInfoList.add(caseInfo);
                caseInfoReturnList.add(caseInfoReturn);
            }
            caseInfoRepository.save(caseInfoList);
            caseInfoReturnRepository.save(caseInfoReturnList);
            log.debug("案件自动回收任务调度结束...");
        } catch (Exception e) {
            log.error("案件自动回收任务调度错误");
            log.error(e.getMessage(), e);
        }

        try {
            log.debug("委外案件自动回收任务调度开始...");
            // 委外自动回收
            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            Iterable<OutsourcePool> outsourcePools = outsourcePoolRepository.findAll(qOutsourcePool.caseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()) // 未回收的
                    .and(qOutsourcePool.caseInfo.closeDate.before(new Date())) // 到期的
                    .and(qOutsourcePool.outStatus.ne(OutsourcePool.OutStatus.OUTSIDE_OVER.getCode())));// 除过委外已结案的
            Iterator<OutsourcePool> iterator1 = outsourcePools.iterator();
            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseInfoReturn> caseInfoReturnList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            while (iterator1.hasNext()) {
                OutsourcePool outsourcePool = iterator1.next();
                outsourcePoolList.add(outsourcePool);

                CaseInfo caseInfo = outsourcePool.getCaseInfo();
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());
                caseInfoList.add(caseInfo);

                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setReason("案件到期自动回收");
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturn.setSource(CaseInfoReturn.Source.OUTSOURCE.getValue()); // 回收来源-委外
                caseInfoReturn.setCaseId(caseInfo);
                caseInfoReturn.setOutBatch(outsourcePool.getOutBatch());
                caseInfoReturn.setOutsName(outsourcePool.getOutsource().getOutsName());
                caseInfoReturn.setOutTime(outsourcePool.getOutTime());
                caseInfoReturn.setOverOutsourceTime(outsourcePool.getOverOutsourceTime());
                caseInfoReturn.setCompanyCode(outsourcePool.getCompanyCode());
                caseInfoReturnList.add(caseInfoReturn);
            }
            caseInfoRepository.save(caseInfoList);
            caseInfoReturnRepository.save(caseInfoReturnList);
            outsourcePoolRepository.delete(outsourcePoolList);
            log.debug("委外案件自动回收任务调度结束...");
        } catch (Exception e) {
            log.error("委外案件自动回收任务调度错误");
            log.error(e.getMessage(), e);
        }

        try {
            log.debug("案件手动回收提醒任务调度开始...");
            // 内催的手动回收
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            Iterable<CaseInfo> allM = caseInfoRepository.findAll(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue())
                    .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())//除过已结案
                            .and(qCaseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()))//未回收的
                            .and(qCaseInfo.recoverWay.eq(CaseInfo.RecoverWay.MANUAL.getValue()))//需要手动回收的
                            .and(qCaseInfo.closeDate.before(new Date()))));//到期日期
            Iterator<CaseInfo> iteratorM = allM.iterator();
            while (iteratorM.hasNext()) {
                CaseInfo caseInfo = iteratorM.next();
                if (Objects.nonNull(caseInfo.getCurrentCollector())) {
                    String id = caseInfo.getCurrentCollector().getId();
                    String title = "案件到期";
                    String content = "案件编号[".concat(caseInfo.getCaseNumber()).concat("],批次号[").concat(caseInfo.getBatchNumber()).concat("]的案件到期,请进行回收。");
                    SendReminderMessage sendReminderMessage = new SendReminderMessage();
                    sendReminderMessage.setTitle(title);
                    sendReminderMessage.setContent(content);
                    sendReminderMessage.setType(ReminderType.CASE_EXPIRE);
                    sendReminderMessage.setMode(ReminderMode.POPUP);
                    sendReminderMessage.setCreateTime(new Date());
                    sendReminderMessage.setUserId(id);
                    reminderService.sendReminder(sendReminderMessage);
                }
            }
            log.debug("案件手动回收提醒任务调度结束...");
        } catch (Exception e) {
            log.error("案件自动回收任务调度错误");
            log.error(e.getMessage(), e);
        }
        log.debug("案件回收任务调度结束");
    }
}
