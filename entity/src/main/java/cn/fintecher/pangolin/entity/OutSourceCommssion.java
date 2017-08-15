package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-15-11:43
 */
@Entity
@Table(name = "outsource_commission")
@Data
public class OutSourceCommssion extends BaseEntity {
    @ApiModelProperty("特定公司的标识")
    private String companyCode;

    @ManyToOne
    @JoinColumn(name = "outsId")
    @ApiModelProperty("委外方")
    private Outsource outsource;

    @ApiModelProperty("逾期时段")
    private String overdueTime;

    @ApiModelProperty("回款金额")
    private BigDecimal returnMoney = new BigDecimal(0);

    @ApiModelProperty("回款户数")
    private Integer returnHouseholds;

    @ApiModelProperty("回退金额")
    private BigDecimal rollbackMoney = new BigDecimal(0);

    @ApiModelProperty("回退户数")
    private Integer rollbackHouseholds;

    @ApiModelProperty("修复金额")
    private BigDecimal repairMoney = new BigDecimal(0);

    @ApiModelProperty("回退户数")
    private Integer repairHouseholds;

    @ApiModelProperty("创建人用户id")
    private String operator;

    @ApiModelProperty("创建时间")
    private Date operateTime;

    @ApiModelProperty("描述")
    private String remark;
}
