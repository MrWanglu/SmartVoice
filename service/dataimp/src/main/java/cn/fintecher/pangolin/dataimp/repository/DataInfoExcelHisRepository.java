package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.DataInfoExcelHis;
import cn.fintecher.pangolin.dataimp.entity.QDataInfoExcelHis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 14:33 2017/7/18
 */
public interface DataInfoExcelHisRepository extends MongoRepository<DataInfoExcelHis, String>,QueryDslPredicateExecutor<DataInfoExcelHis>,
        QuerydslBinderCustomizer<QDataInfoExcelHis> {
    @Override
    default void customize(final QuerydslBindings bindings, final QDataInfoExcelHis root) {
    }
}
