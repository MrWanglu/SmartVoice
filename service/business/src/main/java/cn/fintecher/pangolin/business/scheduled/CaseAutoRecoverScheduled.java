package cn.fintecher.pangolin.business.scheduled;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoReturnRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoReturn;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;

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

    @Scheduled(cron = "0 59 23 * * ?")
    private void caseAutoRecoverTask() {
        log.debug("案件自动回收任务调度开始...");
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            Iterable<CaseInfo> all = caseInfoRepository.findAll(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue())//内催
                    .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//除过已结案
                    .and(qCaseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()))//未回收的
                    .and(qCaseInfo.recoverWay.eq(CaseInfo.RecoverWay.AUTO.getValue()))//需要自动回收的
                    .and(qCaseInfo.closeDate.before(new Date())));//到期日期
            Iterator<CaseInfo> iterator = all.iterator();
            while (iterator.hasNext()) {
                CaseInfo caseInfo = iterator.next();
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());
                caseInfoRepository.save(caseInfo);
                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setCaseId(caseInfo);
                caseInfoReturn.setReason("案件到期自动回收");
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturnRepository.save(caseInfoReturn);
            }
        } catch (Exception e) {
            log.error("案件自动回收任务调度错误");
            log.error(e.getMessage(), e);
        }
        log.debug("案件自动回收任务调度结束");
    }
}
