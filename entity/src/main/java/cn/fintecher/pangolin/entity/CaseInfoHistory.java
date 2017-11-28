package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "case_info_history")
@Data
public class CaseInfoHistory extends BaseEntity {
    @ApiModelProperty(notes = "原案件的id")
    private String caseId;

    @ApiModelProperty(notes = "案件批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "催收类型(电催、外访、司法、委外、提醒)")
    private Integer collectionType;

    @ApiModelProperty(notes = "合同编号")
    private String contractNumber;

    @ApiModelProperty(notes = "合同金额")
    private BigDecimal contractAmount;

    @ApiModelProperty(notes = "逾期总金额")
    private BigDecimal overdueAmount;

    @ApiModelProperty(notes = "逾期本金")
    private BigDecimal overdueCapital;

    @ApiModelProperty(notes = "逾期利息")
    private BigDecimal overdueInterest;

    @ApiModelProperty(notes = "逾期罚息")
    private BigDecimal overdueFin;

    @ApiModelProperty(notes = "逾期滞纳金")
    private BigDecimal overdueDelayFine;

    @ApiModelProperty(notes = "还款期数")
    private Integer periods;

    @ApiModelProperty(notes = "每期还款日")
    private Date perDueDate; //逾期日期

    @ApiModelProperty(notes = "每期还款金额")
    private BigDecimal perPayAmount;

    @ApiModelProperty(notes = "逾期期数")
    private Integer overduePeriods;

    @ApiModelProperty(notes = "逾期天数")
    private Integer overdueDays;

    @ApiModelProperty(notes = "逾期日期")
    private Date overDueDate;

    @ApiModelProperty(notes = "已还款金额")
    private BigDecimal hasPayAmount = new BigDecimal(0); //逾期已还款金额    private Integer hasPayPeriods;

    @ApiModelProperty(notes = "最近还款日期")
    private Date latelyPayDate;

    @ApiModelProperty(notes = "最近还款金额")
    private BigDecimal latelyPayAmount;

    @ApiModelProperty(notes = "协催标识：0-未协催，1-协催")
    private Integer assistFlag;

    @ApiModelProperty(notes = "协催状态")
    private Integer assistStatus;

    @ApiModelProperty(notes = "协催方式")
    private Integer assistWay;

    @ApiModelProperty(notes = "持案天数")
    private Integer holdDays;

    @ApiModelProperty(notes = "剩余天数")
    private Integer leftDays;

    @ApiModelProperty(notes = "案件类型")
    private Integer caseType;

    @ApiModelProperty(notes = "协催标识：0-未留案，1-留案")
    private Integer leaveCaseFlag = 0;

    @ApiModelProperty(notes = "留案操作日期")
    private Date leaveDate;

    @ApiModelProperty(notes = "已留案天数")
    private Integer hasLeaveDays;

    @ApiModelProperty(notes = "案件流转次数")
    private Integer followUpNum = 0;

    @ApiModelProperty(notes = "流入时间")
    private Date caseFollowInTime;

    @ApiModelProperty(notes = "还款状态")
    private String payStatus;

    @ApiModelProperty(notes = "订单ID")
    private String orderId;

    @ApiModelProperty(notes = "催收状态")
    private Integer collectionStatus;

    @ApiModelProperty(notes = "委案日期")
    private Date delegationDate;

    @ApiModelProperty(notes = "结案日期")
    private Date closeDate;

    @ApiModelProperty(notes = "佣金比例")
    private BigDecimal commissionRate;

    @ApiModelProperty(notes = "案件手数")
    private Integer handNumber;

    @ApiModelProperty(notes = "贷款日期")
    private Date loanDate;

    @ApiModelProperty(notes = "逾期管理费")
    private BigDecimal overdueManageFee;

    @ApiModelProperty(notes = "挂起标识")
    private Integer handUpFlag;

    @ApiModelProperty(notes = "逾期减免金额")
    private BigDecimal derateAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "逾期实际还款金额")
    private BigDecimal realPayAmount = new BigDecimal(0);

    @ApiModelProperty(notes = "提前结清已还款金额")
    private BigDecimal earlySettleAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "提前结清实际还款金额")
    private BigDecimal earlyRealSettleAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "提前结清减免金额")
    private BigDecimal earlyDerateAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "其他金额")
    private BigDecimal otherAmt;

    @ApiModelProperty(notes = "案件评分")
    private BigDecimal score;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "剩余本金")
    private BigDecimal leftCapital;

    @ApiModelProperty(notes = "剩余利息")
    private BigDecimal leftInterest;

    @ApiModelProperty(notes = "结案说明")
    private String endRemark;

    @ApiModelProperty(notes = "结案方式")
    private Integer endType;

    @ApiModelProperty(notes = "最新跟进时间")
    private Date followupTime;

    @ApiModelProperty(notes = "催收反馈")
    private Integer followupBack;

    @ApiModelProperty(notes = "承诺还款金额")
    private BigDecimal promiseAmt;

    @ApiModelProperty(notes = "承诺还款日期")
    private Date promiseTime;

    @ApiModelProperty(notes = "授信金额")
    private BigDecimal creditAmount;

    @ApiModelProperty(notes = "流转审批状态")
    private Integer circulationStatus;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "打标标记")
    private Integer caseMark = 126;

    @ApiModelProperty(notes = "备注")
    private String memo;

    @ApiModelProperty(notes = "首次还款日期")
    private Date firstPayDate;

    @ApiModelProperty(notes = "账龄")
    private String accountAge;

    @ApiModelProperty(notes = "案件到期回收方式：0-自动回收，1-手动回收")
    private Integer recoverWay;

    @ApiModelProperty(notes = "回收标志：0-未回收，1-已回收")
    private Integer recoverRemark;

    @ApiModelProperty(notes = "内催 225 委外 226 司法 227 核销 228")
    private Integer casePoolType;

    @ManyToOne
    @JoinColumn(name = "personal_id")
    @ApiModelProperty(notes = "客户信息ID")
    private Personal personalInfo;

    @ManyToOne
    @JoinColumn(name = "depart_id")
    @ApiModelProperty(notes = "部门ID")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "area_id")
    @ApiModelProperty(notes = "省份编号")
    private AreaCode area;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @ApiModelProperty(notes = "")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "lately_collector")
    @ApiModelProperty(notes = "产品名称ID")
    private User latelyCollector;

    @ManyToOne
    @JoinColumn(name = "current_collector")
    @ApiModelProperty(notes = "当前催员")
    private User currentCollector;

    @ManyToOne
    @JoinColumn(name = "assist_collector")
    @ApiModelProperty(notes = "协催员")
    private User assistCollector;

    @ManyToOne
    @JoinColumn(name = "principal_id")
    @ApiModelProperty(notes = "委托方ID")
    private Principal principalId;

    @ManyToOne
    @JoinColumn(name = "operator")
    @ApiModelProperty(notes = "操作员")
    private User operator;

    @OneToMany
    @JoinColumn(name = "caseId")
    @ApiModelProperty(notes = "案件修复ID")
    private List<CaseRepairRecord> caseRepairRecordList;

    /**
     * @Description 催收类型枚举类
     */
    public enum CollectionType {
        //电催
        TEL(15, "电催"),
        //外访
        VISIT(16, "外访"),
        //司法
        JUDICIAL(17, "司法"),
        //委外
        outside(18, "委外"),
        //提醒
        remind(19, "提醒");

        private Integer value;
        private String remark;

        CollectionType(Integer value, String remark) {
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

    /**
     * @Description 催收状态枚举类
     */
    public enum CollectionStatus {

        //待催收
        WAITCOLLECTION(20, "待催收"),
        //催收中
        COLLECTIONING(21, "催收中"),
        //逾期还款中
        OVER_PAYING(22, "逾期还款中"),
        //提前结清还款中
        EARLY_PAYING(23, "提前结清还款中"),
        //已结案
        CASE_OVER(24, "已结案"),
        //待分配
        WAIT_FOR_DIS(25, "待分配"),
        //已委外
        CASE_OUT(166, "已委外"),
        //已还款
        REPAID(171, "已还款"),
        //部分已还款
        PART_REPAID(172, "部分已还款");

        private Integer value;
        private String remark;

        CollectionStatus(Integer value, String remark) {
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

    /**
     * 案件协催状态
     */
    public enum AssistStatus {

        ASSIST_APPROVEING(26, "协催审批中"),
        ASSIST_REFUSED(27, "协催拒绝"),
        ASSIST_COLLECTING(28, "协催催收中"),
        ASSIST_COMPLATED(29, "协催完成"),
        ASSIST_WAIT_ASSIGN(117, "协催待分配"),
        ASSIST_WAIT_ACC(118, "协催待催收"),
        FAILURE(30, "协催审批失效");

        private Integer value;
        private String remark;

        AssistStatus(Integer value, String remark) {
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

    /**
     * @Description 挂起状态枚举类
     */
    public enum HandUpFlag {
        //未挂起
        NO_HANG(52, "未挂起"),
        //挂起
        YES_HANG(53, "挂起");
        private Integer value;

        private String remark;

        HandUpFlag(Integer value, String remark) {
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

    /**
     * @Description 结案方式枚举类
     */
    public enum EndType {
        //已还款
        REPAID(110, "已还款"),
        //司法结案
        JUDGMENT_CLOSED(111, "司法结案"),
        //债主死亡
        CREDITOR_DIED(112, "债主死亡"),
        //批量结案
        BATCH_CLOSURE(113, "批量结案"),
        //委外结案
        OUTSIDE_CLOSED(114, "委外结案");
        private Integer value;

        private String remark;

        EndType(Integer value, String remark) {
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

    /**
     * @Description 打标颜色枚举类
     */
    public enum Color {
        //无色
        NO_COLOR(126, "无色"),
        //红色
        RED(127, "红色"),
        //蓝色
        BLUE(128, "蓝色"),
        //绿色
        GREEN(129, "绿色");

        private Integer value;

        private String remark;

        Color(Integer value, String remark) {
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

    public enum AssistFlag {
        //非协催
        NO_ASSIST(0, "非协催"),
        //协催
        YES_ASSIST(1, "协催");
        private Integer value;

        private String remark;

        AssistFlag(Integer value, String remark) {
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

    /**
     * 案件流转类型
     */
    public enum CaseType {
        DISTRIBUTE(173, "案件分配"), PHNONESMALLTURN(174, "电催小流转"), PHNONEFORCETURN(175, "电催强制流转"), PHNONEFAHEADTURN(176, "电催提前流转"),
        PHNONELEAVETURN(177, "电催保留流转"), OUTSMALLTURN(178, "外访小流转"), OUTFAHEADTURN(179, "外访提前流转"), OUTFORCETURN(180, "外访强制流"),
        OUTLEAVETURN(181, "外访保留流转");
        private Integer value;

        private String remark;

        CaseType(Integer value, String remark) {
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

    /**
     * @还款状态枚举类
     */
    public enum PayStatus {
        //M1
        M1(190, "M1"),
        //M2
        M2(191, "M2"),
        //M3
        M3(192, "M3"),
        //M4
        M4(193, "M4"),
        //M5
        M5(194, "M5"),
        //M6+
        M6_PLUS(195, "M6");
        private Integer value;

        private String remark;

        PayStatus(Integer value, String remark) {
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

    /**
     * 留案标志
     */
    public enum leaveCaseFlagEnum {
        //非留案
        NO_LEAVE(0, "非留案"),
        //留案
        YES_LEAVE(1, "留案");
        private Integer value;

        private String remark;

        leaveCaseFlagEnum(Integer value, String remark) {
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

    /**
     * @Description 流转审批状态
     */
    public enum CirculationStatus {
        //电催流转待审批
        PHONE_WAITING(197, "电催流转待审批"),
        //电催流转通过
        PHONE_PASS(198, "电催流转通过"),
        //电催流转拒绝
        PHONE_REFUSE(199, "电催流转拒绝"),
        //外访流转待审批
        VISIT_WAITING(200, "外访流转待审批"),
        //外访流转通过
        VISIT_PASS(201, "外访流转通过"),
        //外访流转拒绝
        VISIT_REFUSE(202, "外访流转拒绝");
        private Integer value;

        private String remark;

        CirculationStatus(Integer value, String remark) {
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
