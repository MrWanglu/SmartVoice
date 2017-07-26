package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 跟进记录实体
 * @Date : 15:49 2017/7/19
 */

@Entity
@Table(name = "case_followup_record")
@Data
public class CaseFollowupRecord extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "personal_id")
    @ApiModelProperty(notes = "客户信息")
    private Personal personalId;

    @ManyToOne
    @JoinColumn(name = "case_id")
    @ApiModelProperty(notes = "案件信息")
    private CaseInfo caseId;

    @ApiModelProperty(notes = "跟进对象")
    private Integer target;

    @ApiModelProperty(notes = "跟进方式")
    private Integer type;

    @ApiModelProperty(notes = "跟进内容")
    private String content;

    @ApiModelProperty(notes = "电话联系状态")
    private Integer contactState;

    @ApiModelProperty(notes = "联系电话")
    private String contactPhone;

    @ApiModelProperty(notes = "催收类型")
    private Integer collectionType;

    @ApiModelProperty(notes = "催收反馈")
    private Integer collectionFeedback;

    @ApiModelProperty(notes = "数据来源")
    private Integer source;

    @ManyToOne
    @JoinColumn(name = "operator")
    @ApiModelProperty(notes = "操作员")
    private User operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "承诺还款标识 0-没有承诺 1-有承诺")
    private Integer promiseFlag;

    @ApiModelProperty(notes = "承诺还款金额")
    private BigDecimal promiseAmt;

    @ApiModelProperty(notes = "承诺还款日期")
    private Date promiseDate;

    @ApiModelProperty(notes = "下次跟进提醒标志 0-没有下次跟进 1-有下次跟进")
    private Integer follnextFlag;

    @ApiModelProperty(notes = "下次跟进提醒日期")
    private Date follnextDate;

    @ApiModelProperty(notes = "下次跟进提醒内容")
    private String follnextContent;

    @ApiModelProperty(notes = "地址类型")
    private Integer addrType;

    @ApiModelProperty(notes = "通话ID")
    private String taskId;

    @ApiModelProperty(notes = "通话记录ID")
    private String recoderId;

    @ApiModelProperty(notes = "主叫id")
    private String taskcallerId;

    @ApiModelProperty(notes = "录音地址")
    private String opUrl;

    @ApiModelProperty(notes = "录音下载标识")
    private Integer loadFlag;

    @ApiModelProperty(notes = "催记方式 0-自动 1-手动")
    private Integer collectionWay;

    @ApiModelProperty(notes = "定位地址")
    private String collectionLocation;

    @ApiModelProperty(notes = "外访资料id集合")
    @Transient
    private List fileIds;

    /**
     * @Description 电话状态枚举类
     */
    public enum ContactState {
        //正常
        NORMAL(64, "正常"),
        //空号
        UNN(65, "空号"),
        //停机
        HALT(66, "停机"),
        //关机
        POWEROFF(67, "关机"),
        //未知
        UNKNOWN(68, "未知");

        private Integer value;

        private String remark;

        ContactState(Integer value, String remark) {
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
     * @Description 跟进对象枚举类
     */
    public enum Target {
        //本人
        SELF(69, "本人"),
        //配偶
        SPOUSE(70, "配偶"),
        //父母
        PARENTS(71, "父母"),
        //子女
        CHILD(72, "子女"),
        //亲属
        RELATIVES(73, "亲属"),
        //同事
        COLLEAGUE(74, "同事"),
        //朋友
        FRIEND(75, "朋友"),
        //其他
        OTHER(76, "其他"),
        //单位
        UNIT(77, "单位");

        private Integer value;

        private String remark;

        Target(Integer value, String remark) {
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
     * @Description 跟进方式枚举类
     */
    public enum Type {
        //电话
        TEL(78, "电话"),
        //外访
        VISIT(79, "外访"),
        ASSIST(162, "协催");
        private Integer value;

        private String remark;

        Type(Integer value, String remark) {
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
     * @Description 数据来源枚举类
     */
    public enum Source {
        //电话
        TEL(80, "电话"),
        //外访
        VISIT(81, "外访"),
        //信贷
        LOAN(82, "信贷");
        private Integer value;

        private String remark;

        Source(Integer value, String remark) {
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
     * @Description 地址类型枚举类
     */
    public enum AddrType {
        //现居住地址
        LIVING_ADDRESS(83, "现居住地址"),
        //单位地址
        COMPANY_ADDRESS(84, "单位地址"),
        //身份证地址
        IDCARD_ADDRESS(85, "身份证地址"),
        //房产地址
        ESTATE_ADDRESS(86, "房产地址"),
        //其他
        OTHER_ADDRESS(87, "其他");

        private Integer value;

        private String remark;

        AddrType(Integer value, String remark) {
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
     * @Description 联络类型枚举类
     */
    public enum CollectionType {
        //有效联络
        EFFECTIVE_COLLECTION(88, "有效联络"),
        //无效联络
        INVALID_COLLECTION(89, "无效联络");

        private Integer value;

        private String remark;

        CollectionType(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }
    }

    /**
     * @Description 有效联络枚举类
     */
    public enum EffectiveCollection {
        //承诺还款
        PROMISE(90, "承诺还款"),
        //协商跟进
        CONSULT(91, "协商跟进"),
        //拒绝还款
        REFUSEPAY(92, "拒绝还款"),
        //客户提示已还款
        HAVEREPAYMENT(93, "客户提示已还款");

        private Integer value;

        private String remark;

        EffectiveCollection(Integer value, String remark) {
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
     * @Description 无效联络枚举类
     */
    public enum InvalidCollection {
        //他人转告
        OTHERTELL(94, "他人转告"),
        //查找
        FIND(95, "查找"),
        //无人应答
        NOANSWER(96, "无人应答"),
        //空坏号
        EMPTYORBAD(97, "空坏号"),
        //无法接通
        NOCONNECTION(98, "无法联通"),
        //失联
        LOST(99, "失联"),
        //待核实
        VERIFICATING(100, "待核实");

        private Integer value;

        private String remark;

        InvalidCollection(Integer value, String remark) {
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