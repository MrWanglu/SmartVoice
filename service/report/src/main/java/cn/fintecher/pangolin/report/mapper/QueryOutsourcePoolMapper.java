package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import cn.fintecher.pangolin.report.model.QueryOutsourcePool;
import cn.fintecher.pangolin.report.model.QueryOutsourcePoolParams;
import cn.fintecher.pangolin.report.util.MyMapper;

import java.util.List;

/**
 * @author : huyanmin
 * @Description : 委外催收
 * @Date : 2017/9/25
 */

public interface QueryOutsourcePoolMapper extends MyMapper<BackMoneyReport> {
    /**
     * @Description 委外催收中按批次号查询
     */
    List<QueryOutsourcePool> getAllOutSourcePoolByBatchNumber(QueryOutsourcePoolParams queryOutsourcePoolParams);

    /**
     * @Description 委外催收中按委外方查询
     */
    List<QueryOutsourcePool> getAllOutSourceByOutsName(QueryOutsourcePoolParams queryOutsourcePoolParams);
}
