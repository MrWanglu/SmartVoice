package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 案件流转记录实体
 * @Date : 15:48 2017/7/18
 */

@Entity
@Table(name = "case_turn_record")
@Data
public class CaseTurnRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(notes = "主键ID")
    private Integer id;

    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "催收类型")
    private Integer collectionType;

    @ApiModelProperty(notes = "催收状态")
    private Integer collectionStatus;

    @ApiModelProperty(notes = "实际还款金额")
    private BigDecimal realPayAmount;

    @ApiModelProperty(notes = "合同金额")
    private BigDecimal contractAmount;

    @ApiModelProperty(notes = "提前结清实际还款金额")
    private BigDecimal earlyRealsettleAmt;

    @ApiModelProperty(notes = "部门ID")
    private String departId;

    @ApiModelProperty(notes = "协催方式")
    private Integer assistWay;

    @ApiModelProperty(notes = "协催标识")
    private Integer assistFlag;

    @ApiModelProperty(notes = "持案天数")
    private Integer holdDays;

    @ApiModelProperty(notes = "剩余天数")
    private Integer leftDays;

    @ApiModelProperty(notes = "案件类型")
    private Integer caseType;

    @ApiModelProperty(notes = "当前催收员ID")
    private String currentCollector;

    @ApiModelProperty(notes = "接收人ID")
    private String receiveUserId;

    @ApiModelProperty(notes = "接受人名称")
    private String receiveUserRealName;

    @ApiModelProperty(notes = "案件流转次数")
    private Integer followUpNum;

    @ApiModelProperty(notes = "接受部门名称")
    private String receiveDeptName;

    @ApiModelProperty(notes = "操作员")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "流转类型 0-自动流转 1-手动流转 2-正常流转")
    private Integer circulationType;
}