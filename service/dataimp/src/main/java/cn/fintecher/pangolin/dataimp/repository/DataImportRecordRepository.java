package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.DataImportRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:47 2017/7/18
 */
public interface DataImportRecordRepository extends MongoRepository<DataImportRecord, String>,QueryDslPredicateExecutor<DataImportRecord> {
}
