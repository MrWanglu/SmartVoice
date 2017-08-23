package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.BatchManage;
import cn.fintecher.pangolin.entity.QBatchManage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:57 2017/5/17
 */

public interface BatchManageRepository extends QueryDslPredicateExecutor<BatchManage>, JpaRepository<BatchManage, String>, QuerydslBinderCustomizer<QBatchManage> {
    @Override
    default void customize(final QuerydslBindings bindings, final QBatchManage root) {

    }

//    SELECT
//    QRTZ_JOB_DETAILS.JOB_NAME AS '任务名称',
//    QRTZ_JOB_DETAILS.JOB_GROUP AS '任务所在组',
//    QRTZ_JOB_DETAILS.DESCRIPTION AS '任务描述',
//    QRTZ_JOB_DETAILS.JOB_CLASS_NAME AS '任务类名',
//    QRTZ_TRIGGERS.TRIGGER_NAME AS '触发器名称',
//    QRTZ_TRIGGERS.TRIGGER_GROUP AS '触发器所在组',
//    QRTZ_TRIGGERS.NEXT_FIRE_TIME AS '下次执行时间',
//    QRTZ_CRON_TRIGGERS.CRON_EXPRESSION AS '表达式',
//    QRTZ_CRON_TRIGGERS.TIME_ZONE_ID AS '时区'
//    FROM
//            QRTZ_JOB_DETAILS, QRTZ_TRIGGERS, QRTZ_CRON_TRIGGERS
//    WHERE QRTZ_JOB_DETAILS.JOB_NAME = QRTZ_TRIGGERS.JOB_NAME
//    AND QRTZ_TRIGGERS.TRIGGER_NAME = QRTZ_CRON_TRIGGERS.TRIGGER_NAME
//    AND QRTZ_TRIGGERS.TRIGGER_GROUP = QRTZ_CRON_TRIGGERS.TRIGGER_GROUP
//    AND QRTZ_JOB_DETAILS.JOB_NAME LIKE '%0001'

    @Query(value = "SELECT " +
            "    QRTZ_JOB_DETAILS.JOB_NAME AS taskName, " +
            "    QRTZ_JOB_DETAILS.JOB_GROUP AS taskGroup, " +
            "    QRTZ_JOB_DETAILS.DESCRIPTION AS taskDescription, " +
            "    QRTZ_JOB_DETAILS.JOB_CLASS_NAME AS taskClassName, " +
            "    QRTZ_TRIGGERS.TRIGGER_NAME AS triggerName, " +
            "    QRTZ_TRIGGERS.TRIGGER_GROUP AS triggerGroup, " +
            "    QRTZ_TRIGGERS.NEXT_FIRE_TIME AS nextExecutionTime, " +
            "    QRTZ_CRON_TRIGGERS.CRON_EXPRESSION AS expression, " +
            "    QRTZ_CRON_TRIGGERS.TIME_ZONE_ID AS timeZone " +
            "            FROM " +
            "    QRTZ_JOB_DETAILS, QRTZ_TRIGGERS, QRTZ_CRON_TRIGGERS  " +
            "    WHERE QRTZ_JOB_DETAILS.JOB_NAME = QRTZ_TRIGGERS.JOB_NAME " +
            "    AND QRTZ_TRIGGERS.TRIGGER_NAME = QRTZ_CRON_TRIGGERS.TRIGGER_NAME " +
            "    AND QRTZ_TRIGGERS.TRIGGER_GROUP = QRTZ_CRON_TRIGGERS.TRIGGER_GROUP " +
            "    AND QRTZ_JOB_DETAILS.JOB_NAME LIKE  concat('%',:companyCode)", nativeQuery = true)
    Object[] batchManageFind(@Param("companyCode") String companyCode);
}
