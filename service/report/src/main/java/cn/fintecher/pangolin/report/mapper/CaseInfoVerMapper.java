package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.CaseInfoVerification;
import cn.fintecher.pangolin.report.model.CaseInfoVerModel;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;
import java.util.List;

/**
 * @author : yuanyanting
 * @Description : 核销管理报表
 * @Date : 19:37 2017/9/1
 */
public interface CaseInfoVerMapper extends BaseMapper<CaseInfoVerification> {
    /**
     * @Descritpion 查询核销管理
     */
    List<CaseInfoVerModel> getCaseInfoVerReport(@Param("startTime") String startTime,
                                                @Param("endTime") String endTime,
                                                @Param("page") Integer page,
                                                @Param("size") Integer size,
                                                @Param("companyCode") String companyCode);
}
