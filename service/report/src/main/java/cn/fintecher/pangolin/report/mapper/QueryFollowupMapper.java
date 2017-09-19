package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.model.ExcportResultModel;
import cn.fintecher.pangolin.report.model.ExportFollowRecordParams;
import cn.fintecher.pangolin.report.model.ExportFollowupParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author : baizhangyu
 * @Description : 管理员首页
 * @Date : 2017/8/8.
 */
public interface QueryFollowupMapper {

    /**
     * 获取导出跟进记录数据
     * @param company，list
     * @return
     */
    List<ExportFollowupParams> getExportFollowModel(@Param("list")List list, @Param("company")String company);


    List<ExcportResultModel> findFollowupRecord(ExportFollowRecordParams exportFollowupParams);

}
