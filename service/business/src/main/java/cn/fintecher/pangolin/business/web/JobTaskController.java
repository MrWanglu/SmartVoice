package cn.fintecher.pangolin.business.web;

/**
 * @Author: PeiShouWen
 * @Description: 任务调度
 * @Date 15:21 2017/8/10
 */

import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

@RestController
@RequestMapping("/api/jobTaskController")
@Api(value = "JobTaskController", description = "任务调度")
public class JobTaskController extends BaseController {
    private final Logger logger= LoggerFactory.getLogger(JobTaskController.class);
    private static final String ENTITY_NAME = "JobTaskController";

    @Autowired
    EntityManager entityManager;

    @GetMapping("/updateOverNightJob")
    public ResponseEntity updateOverNightJob(){
        /*QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        queryFactory.select(qCaseInfo.id,qCaseFollowupRecord).from(qCaseInfo).leftJoin(qCaseFollowupRecord).on(qCaseInfo.id.eq(qCaseFollowupRecord.caseId)).fetchResults();
       *//* jpaQuery.where(qCaseInfo.leaveCaseFlag.eq(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())//未留案
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//未结案
                .and(qCaseInfo.currentCollector.isNotNull())//催收员不为空
                .and(qCaseInfo.collectionType.eq(15))//催收类型
                .and(qCaseInfo.holdDays.goe(5))//持有天数大于等于配置参数
                .and(qCaseFollowupRecord.collectionWay.eq(CaseFollowupRecord.CollectionWayEnum.MANUAL.getValue()))//跟进记录为手动
                .and(qCaseFollowupRecord.operatorTime.goe(qCaseInfo.caseFollowInTime))//跟进日期大于等于流入日期
                .and(qCaseFollowupRecord.id.isNull())//无跟进记录的案件
                .and(qCaseInfo.companyCode.eq("0001")));//公司码*//*
      //jpaQuery.fetch();*/
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"updateOverNightJob","更新成功")).body(null);
    }
}
