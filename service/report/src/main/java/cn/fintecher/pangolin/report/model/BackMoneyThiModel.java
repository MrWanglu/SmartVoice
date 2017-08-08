package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 回款信息三级模型
 * @Date : 14:38 2017/8/2
 */

@Data
public class BackMoneyThiModel {
    private String userName; //用户名
    private String code; //部门code码
    private List<BackMoneyReport> backMoneyReports; //用户回款报表集合
}