package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.model.ExcportOutsourceResultModel;
import cn.fintecher.pangolin.report.model.ExportOutsourceFollowRecordParams;

import java.util.List;

/**
 * @Author : huyanmin
 * @Description : 导出委外跟进记录
 * @Date : 2017/9/27.
 */
public interface QueryOutsourceFollowupMapper {

    /**
     * 导出委外跟进记录数据
     * @return
     */
    List<ExcportOutsourceResultModel> findOutsourceFollowupRecord(ExportOutsourceFollowRecordParams exportFollowupParams);

    /**
     * 导出委外案件数据
     * @return
     */
    List<ExcportOutsourceResultModel> findOutsourceRecord(ExportOutsourceFollowRecordParams exportFollowupParams);


}
