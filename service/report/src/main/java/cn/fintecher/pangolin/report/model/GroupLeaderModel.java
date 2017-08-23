package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 小组汇总展示模型
 * @Date : 15:07 2017/8/22
 */

@Data
public class GroupLeaderModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private Integer caseNum = 0; //案件数量
    private Integer manageNum = 0; //管理人数
    private String manageName; //组长
    private Date nowDate; //报表日期
    private BigDecimal dayBackMoney = new BigDecimal(0); //当日回款金额
    private BigDecimal monthBackMoney = new BigDecimal(0); //月累计金额
    private BigDecimal averageMoney = new BigDecimal(0); //人均回款金额
    private Integer rank; //排名
    private BigDecimal target = new BigDecimal(0); //月度回款金额目标
    private BigDecimal targetDisparity = new BigDecimal(0); //月度目标差距
    private String companyCode; //公司code码
}