package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.TemplateDataModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 16:15 2017/7/18
 */
public interface TemplateDataModelRepository extends MongoRepository<TemplateDataModel, String>,QueryDslPredicateExecutor<TemplateDataModel> {
}
