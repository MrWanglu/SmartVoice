package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.OutsourceFollowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface OutsourceFollowupRecordRepository extends QueryDslPredicateExecutor<OutsourceFollowRecord>, JpaRepository<OutsourceFollowRecord, String> {
}
