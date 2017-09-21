package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QTemplate;
import cn.fintecher.pangolin.entity.Template;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.List;

/**
 * Created by luqiang on 2017/7/24.
 */
public interface TemplateRepository extends QueryDslPredicateExecutor<Template>, JpaRepository<Template, String>, QuerydslBinderCustomizer<QTemplate> {
    @Override
    default void customize(final QuerydslBindings bindings, final QTemplate root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
    }

    List<Template> findByTemplateNameOrTemplateCodeAndCompanyCode(String companyCode,String templateName, String templateCode);
    List<Template> findByTemplateNameAndCompanyCode(String templateName, String companyCode);
    List<Template> findByTemplateCodeAndCompanyCode(String templateName, String companyCode);

    List<Template> findTemplatesByTemplateStyleAndTemplateTypeAndCompanyCode(int templateStyle, int type, String companyCode);

    List<Template> findTemplatesByTemplateStyleAndTemplateTypeAndTemplateStatusAndIsDefaultAndCompanyCode(int templateStyle, int templateType, int templateStatus, boolean isDefault, String compCode);

    List<Template> findTemplatesByTemplateStyleAndTemplateTypeAndTemplateName(int templateStyle, int templateType, String templateName);

}
