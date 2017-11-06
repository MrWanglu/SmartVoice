package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.CaseInfoDistributed;
import cn.fintecher.pangolin.report.model.CaseInfoDistributeQueryParams;

import java.util.List;

/**
 * 待分配案件
 * Created by sunyanping on 2017/11/6.
 */

public interface CaseInfoDistributeMapper {

    /**
     * 查询待分配案件
     * @param params
     * @return
     */
    List<CaseInfoDistributed> findCaseInfoDistribute(CaseInfoDistributeQueryParams params);
}
