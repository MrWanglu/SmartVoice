package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QTemplate;
import cn.fintecher.pangolin.entity.Template;
import com.querydsl.core.types.dsl.StringPath;
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
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
    }

    List<Template> findByTemplateNameOrTemplateCode(String templateName, String templateCode);

    List<Template> findTemplatesByTemplateStyleAndTemplateType(int templateStyle, int type);

    List<Template> findTemplatesByTemplateStyleAndTemplateTypeAndTemplateStatusAndIsDefault(int templateStyle, int templateType, int templateStatus, boolean isDefault);

    List<Template> findTemplatesByTemplateStyleAndTemplateTypeAndTemplateName(int templateStyle, int templateType, String templateName);

}
