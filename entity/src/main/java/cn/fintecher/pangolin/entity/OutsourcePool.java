package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "outsource_pool")
@Data
public class OutsourcePool extends BaseEntity {
    @ManyToOne
    @JoinColumn(name="case_id")
    private CaseInfo caseInfo;

    @ManyToOne
    @JoinColumn(name="out_id")
    private Outsource outsource;

    @ApiModelProperty("委外时间")
    private Date outTime;

    @ApiModelProperty("操作时间")
    private Date operateTime;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("委外状态")
    private Integer outStatus;

    @ApiModelProperty("委外批次号")
    private String outBatch;

    @ApiModelProperty("委外回款金额")
    private BigDecimal outBackAmt = new BigDecimal(0);

    @ApiModelProperty("逾期时段")
    private String overduePeriods = new String("M0");

    @ApiModelProperty("案件金额")
    private BigDecimal contractAmt = new BigDecimal(0);

    //Added by huyanmin 2017/9/5
    @ApiModelProperty("委外操作状态")
    private Integer outoperationStatus; //回款 204，回退 205，修复 206

    //Added by huyanmin 2017/9/25
    @ApiModelProperty("公司标识符")
    private String companyCode;

    //Added by huyanmin 2017/9/25
    @ApiModelProperty("委外到期时间")
    private Date overOutsourceTime;

    //Added by huyanmin 2017/9/25
    @ApiModelProperty("委外结案日期")
    private Date endOutsourceTime;

    //Added by huyanmin 2017/9/25
    @ApiModelProperty("佣金比例")
    private BigDecimal commissionRate = new BigDecimal(0.00);

    //Added by huyanmin 2017/9/25
    @ApiModelProperty("佣金金额")
    private BigDecimal commission  = new BigDecimal(0);


    public enum OutStatus {

        //待委外
        TO_OUTSIDE(167, "待委外"),
        //催收中
        OUTSIDING(168, "催收中"),
        //委外到期
        OUTSIDE_EXPIRE(169, "委外到期"),
        //已结案
        OUTSIDE_OVER(170, "已结案");
        private Integer code;
        private String remark;

        OutStatus(Integer code, String remark) {

            this.code = code;
            this.remark = remark;
        }

        public Integer getCode() {
            return code;
        }
        public String getRemark() {
            return remark;
        }
    }
}
