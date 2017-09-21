package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.QOutsourcePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-11:18
 */
public interface OutsourcePoolRepository extends QueryDslPredicateExecutor<OutsourcePool>, JpaRepository<OutsourcePool, String>, QuerydslBinderCustomizer<QOutsourcePool> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourcePool root) {

    }

    /**
     * 获取特定委外方下的特定案件的个数（共债案件）
     *
     * @param name,idCard,outIds
     * @return
     */
    @Query(value = "select a.out_id,COUNT(*) from outsource_pool a " +
            "LEFT JOIN case_info b on a.case_id=b.id " +
            "LEFT JOIN personal c on b.personal_id=c.id " +
            "where c.`name`=:name " +
            "and c.id_card=:idCard " +
            "and a.out_status=168 " +
            "GROUP BY a.out_id", nativeQuery = true)
    Object[] getGzNum(@Param("name") String name,@Param("idCard") String idCard);
}