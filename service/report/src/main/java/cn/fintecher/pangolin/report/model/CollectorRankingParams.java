package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by sunyanping on 2017/11/8.
 */
@Data
@ApiModel("管理员首页催收员排行榜传值参数")
public class CollectorRankingParams {
    @ApiModelProperty(value = "排名类型:0-按回款金额排名,1-按回款案件数排名,2-按回款率排名", required = true)
    private Integer rankType = 0;
    @ApiModelProperty(value = "时间类型:0-年,1-月,2-周", required = true)
    private Integer timeType = 0;
    @ApiModelProperty("部门Code(前端不需要传)")
    private String deptCode;
    @ApiModelProperty(value = "催收类型 0 全部 1 内崔 2 委外 3 核销 4 司法 ", required = true)
    private Integer queryType;
    @ApiModelProperty(value = "查询起始日期 年 20170101 月 20170101 周 20170104", required = true)
    private String startDate;
    @ApiModelProperty(value = "查询终止日期 年 20180101 月 20170201 走 20170112", required = true)
    private String endDate;
    @ApiModelProperty("公司标识码")
    private String companyCode;


    /**
     * 排名类型枚举
     */
    public enum RankType {

        PAY_AMOUNT(0, "按回款金额排名"),
        PAY_COUNT(1, "按回款案件数排名"),
        PAY_RATE(2, "按回款率排名");

        private Integer value;
        private String remark;

        RankType(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }
    }

    /**
     * 按照（年，月，周）展示枚举
     */
    public enum TimeType {
        YEAR(0, "年"),
        MONTH(1, "月"),
        WEEK(2, "周");

        private Integer value;
        private String remark;

        TimeType(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }
    }
}
