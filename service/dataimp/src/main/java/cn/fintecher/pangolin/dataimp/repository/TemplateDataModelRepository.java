package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.QTemplateDataModel;
import cn.fintecher.pangolin.dataimp.entity.TemplateDataModel;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 16:15 2017/7/18
 */
public interface TemplateDataModelRepository extends MongoRepository<TemplateDataModel, String>,QueryDslPredicateExecutor<TemplateDataModel>
        ,QuerydslBinderCustomizer<QTemplateDataModel> {
    @Override
    default void customize(final QuerydslBindings bindings, final QTemplateDataModel root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
    }
    public List<TemplateDataModel> findTemplateByPrincipalNameAndCompanyCode(String principalName,String companyCode);
}
