package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author:liuxiang
 * @Descripion:区域解析DAO层
 * @Date:2017-8-9
 */
public interface AreaReviseDataRepository extends MongoRepository<DataInfoExcel, String>,QueryDslPredicateExecutor<DataInfoExcel>
{

}


