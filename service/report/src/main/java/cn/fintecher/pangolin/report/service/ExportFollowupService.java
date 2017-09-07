package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.report.mapper.QueryFollowupMapper;
import cn.fintecher.pangolin.report.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author : baizhangyu
 * @Description : 导出跟进记录
 * @Date : 2017/9/6.
 */
@Service("exportFollowupService")
public class ExportFollowupService {

    @Autowired
    private QueryFollowupMapper queryFollowupMapper;

    /**
     * 查询跟进数据
     * @param list,company
     * @return
     */
    public List<ExportFollowupParams> getExcelData(List list, String company) {
        return queryFollowupMapper.getExportFollowModel(list, company);
    }
}
