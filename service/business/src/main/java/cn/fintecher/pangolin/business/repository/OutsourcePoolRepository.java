package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.QOutsourcePool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-11:18
 */
public interface OutsourcePoolRepository extends QueryDslPredicateExecutor<OutsourcePool>, JpaRepository<OutsourcePool, String>, QuerydslBinderCustomizer<QOutsourcePool> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourcePool root) {
        /*
        * Added by huyanmin at 2017/09/20
        * */
        //客户姓名模糊查询
        bindings.bind(root.caseInfo.personalInfo.name).first((path, value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        //客户手机号
        bindings.bind(root.caseInfo.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)).or(root.caseInfo.personalInfo.personalContacts.any().phone.eq(StringUtils.trim(value))));
        //客户身份证号
        bindings.bind(root.caseInfo.personalInfo.idCard).first((path, value) -> path.contains(StringUtils.trim(value)));
        //批次号
        bindings.bind(root.caseInfo.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //委案日期
        bindings.bind(root.caseInfo.delegationDate).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            Date firstDelegationDate = it.next();
            if (it.hasNext()) {
                Date secondDelegationDate = it.next();
                return path.between(firstDelegationDate, secondDelegationDate);
            } else {
                //大于等于
                return path.goe(firstDelegationDate);
            }
        });
        //结案日期
        bindings.bind(root.caseInfo.closeDate).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            Date firstCloseDate = it.next();
            if (it.hasNext()) {
                Date secondCloseDate = it.next();
                return path.between(firstCloseDate, secondCloseDate);
            } else {
                //大于等于
                return path.goe(firstCloseDate);
            }
        });

    }

    /**
     * 获取所有相同委托方案件
     *
     * @return
     */
    @Query(value = "SELECT aa.outs_name, MIN(aa.case_follow_in_time), MIN(aa.delegation_date), Max(aa.close_date), MIN(aa.left_days), sum(aa.overdue_amount), count(*) from (SELECT c.outs_name,b.case_follow_in_time,b.overdue_amount,b.delegation_date,b.close_date,b.left_days,a.out_status\n" +
            "FROM outsource c \n" +
            "INNER JOIN outsource_pool a on c.id=a.out_id\n" +
            "INNER JOIN case_info b on a.case_id=b.id) aa GROUP BY aa.outs_name", nativeQuery = true)
    List<Object[]>  findAllOutsourcePoolCase();

    /**
     * 获取所有相同委托方已结案案件
     *
     * @return
     */
    @Query(value = "SELECT c.outs_name, count(*),sum(b.overdue_amount)\n" +
            "FROM outsource c \n" +
            "INNER JOIN outsource_pool a on c.id=a.out_id\n" +
            "INNER JOIN case_info b on a.case_id=b.id where a.out_status=170 GROUP BY c.outs_name", nativeQuery = true)
    List<Object[]>  findAllClosedsourcePoolCase();


    /**
     * 获取所有相同批次号案件
     *
     * @return
     */
    @Query(value = "SELECT aa.outs_name, MIN(aa.case_follow_in_time), MIN(aa.delegation_date), Max(aa.close_date), MIN(aa.left_days), sum(aa.overdue_amount), count(*),aa.out_batch from (SELECT c.outs_name,b.case_follow_in_time,b.overdue_amount,b.delegation_date,b.close_date,b.left_days,a.out_status,a.out_batch\n" +
            "FROM outsource  c\n" +
            "INNER JOIN outsource_pool a on c.id=a.out_id\n" +
            "INNER JOIN case_info b on a.case_id=b.id ORDER BY a.out_batch) aa GROUP BY aa.out_batch, aa.outs_name", nativeQuery = true)
    List<Object[]>  findAllOutsourcePoolBatchCase();

    /**
     * 获取所有相同批次号已结案案件
     *
     * @return
     */
    @Query(value = "SELECT a.out_batch,c.outs_name,count(*),sum(b.overdue_amount)\n" +
            "FROM outsource c\n" +
            "INNER JOIN outsource_pool a on c.id=a.out_id\n" +
            "INNER JOIN case_info b on a.case_id=b.id where a.out_status=170 GROUP BY a.out_batch,c.outs_name ORDER BY a.out_batch", nativeQuery = true)
    List<Object[]>  findAllClosedsourcePoolBatchCase();


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
    Object[] getGzNum(@Param("name") String name, @Param("idCard") String idCard);

}