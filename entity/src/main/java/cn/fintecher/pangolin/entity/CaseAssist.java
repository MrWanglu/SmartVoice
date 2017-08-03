package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 协催案件池
 * @Date : 9:41 2017/7/18
 */

@Entity
@Table(name = "case_assist")
@Data
public class CaseAssist extends BaseEntity {
    @ApiModelProperty("案件信息")
    @ManyToOne
    @JoinColumn(name = "case_id")
    private CaseInfo caseId;
    @ApiModelProperty("协催部门ID")
    private String departId;
    @ApiModelProperty("协催持案天数")
    private Integer holdDays;
    @ApiModelProperty("协催剩余天数")
    private Integer leftDays;
    @ApiModelProperty("留案标志")
    private Integer leaveCaseFlag;
    @ApiModelProperty("已留案天数")
    private Integer hasLeaveDays;
    @ApiModelProperty("打标标记（0-无，1-红色，2-蓝色，3-黄色）")
    private Integer markId;
    @ApiModelProperty("挂起标识")
    private Integer handupFlag;
    @ApiModelProperty("协催公司code码")
    private String companyCode;
    @ApiModelProperty("协催方式")
    private Integer assistWay;
    @ApiModelProperty("协催案件状态")
    private Integer assistStatus;
    @ApiModelProperty("协催结束标识（手动结束，自动结束）")
    private Integer assistCloseFlag;
    @ApiModelProperty("留案操作日期")
    private Date leaveDate;
    @ApiModelProperty("流入时间")
    private Date caseFlowinTime;
    @ApiModelProperty("操作时间")
    private Date operatorTime;
    @ApiModelProperty("协催案件流转时间")
    private Date turnTime;
    @ApiModelProperty("上一个协催员")
    @ManyToOne
    @JoinColumn(name = "lately_collector")
    private User latelyCollector;
    @ApiModelProperty("当前催收员")
    @ManyToOne
    @JoinColumn(name = "current_collector")
    private User currentCollector;
    @ApiModelProperty("协催员")
    @ManyToOne
    @JoinColumn(name = "assist_collector")
    private User assistCollector;
    @ApiModelProperty("操作员")
    @ManyToOne
    @JoinColumn(name = "operator")
    private User operator;

    /**
     * @Description 协催方式枚举
     */
    public enum AssistWay {
        //单次协催
        ONCE_ASSIST(30, "单次协催"),
        //全程协催
        WHOLE_ASSIST(31, "全程协催");
        private Integer value;
        private String remark;

        AssistWay(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }
}