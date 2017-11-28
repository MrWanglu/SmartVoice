package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by huaynmin on 2017/9/26.
 * @Description : 委外跟进记录实体类
 */
@Entity
@Data
@Table(name = "outsource_follow_record")
public class OutsourceFollowRecord extends BaseEntity {
    @ApiModelProperty(notes = "公司标识符")
    private String companyCode;

    @ApiModelProperty(notes = "案件ID")
    @ManyToOne
    @JoinColumn(name="case_id")
    private CaseInfo caseInfo;

    @ApiModelProperty(notes = "案件编号")
    private String caseNum;

    @ApiModelProperty(notes = "跟进时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date followTime;

    @ApiModelProperty(notes = "跟进方式")
    private Integer followType;

    @ApiModelProperty(notes = "催收对象")
    private Integer objectName;

    @ApiModelProperty(notes = "姓名")
    private String userName;

    @ApiModelProperty(notes = "电话状态")
    private Integer telStatus;

    @ApiModelProperty(notes = "催收反馈")
    private Integer feedback;

    @ApiModelProperty(notes = "跟进记录")
    private String followRecord;

    @ApiModelProperty(notes = "跟进人员")
    private String followPerson;

    @ApiModelProperty(notes = "操作人")
    private String operatorName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;


    /**
     * @Description 跟进方式枚举类
     */
    public enum FollowType {
        //电话
        TEL(80, "电话"),
        //外访
        VISIT(81, "外访");

        private Integer value;

        private String remark;

        FollowType(Integer value, String remark) {
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
     * @Description 电话状态枚举类
     */
    public enum TelStatus {
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

        TelStatus(Integer value, String remark) {
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
    public enum ObjectName {
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
        UNIT(77, "单位"),
        //同学
        STUDENT(219, "同学");

        private Integer value;

        private String remark;

        ObjectName(Integer value, String remark) {
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
     * @Description 有效联络枚举类
     */
    public enum FeedBack {
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

        FeedBack(Integer value, String remark) {
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
