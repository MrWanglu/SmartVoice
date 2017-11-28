package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "personal_contact")
@Data
public class PersonalContact extends BaseEntity {
    @ApiModelProperty(notes = "关系：145-本人，146-配偶，147-父母，148-子女，149-亲属，150-同事，151-朋友，152-其他，153-亲兄弟姐妹，154-单位")
    private Integer relation;

    @ApiModelProperty(notes = "联系人姓名")
    private String name;

    @ApiModelProperty(notes = "是否知晓此项借款")
    private Integer informed;

    @ApiModelProperty(notes = "手机号码")
    private String phone;

    @ApiModelProperty(notes = "联系电话状态")
    private Integer phoneStatus;

    @ApiModelProperty(notes = "电子邮箱")
    private String mail;

    @ApiModelProperty(notes = "固定电话")
    private String mobile;

    @ApiModelProperty(notes = "身份证号码")
    private String idCard;

    @ApiModelProperty(notes = "联系人工作单位")
    private String employer;

    @ApiModelProperty(notes = "联系人部门")
    private String department;

    @ApiModelProperty(notes = "联系人职位")
    private String position;

    @ApiModelProperty(notes = "数据来源")
    private Integer source;

    @ApiModelProperty(notes = "联系人的现居住地址")
    private String address;

    @ApiModelProperty(notes = "联系人单位电话")
    private String workPhone;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "社交帐号类型")
    private Integer socialType;

    @ApiModelProperty(notes = "社交帐号内容")
    private String socialValue;

    @ApiModelProperty(notes = "客户信息ID")
    private String personalId;

    @ApiModelProperty(notes = "地址状态")
    private Integer addressStatus;

    /**
     * @Description 社交帐号枚举类
     */
    public enum SocialType {
        //微信
        WECHAT(159, "微信"),
        //QQ
        QQ(160, "QQ"),
        //其他
        OTHER(161, "其他");

        private Integer value;

        private String remark;

        SocialType(Integer value, String remark) {
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
     * @Description 客户关系枚举类
     */
    public enum relation {

        SELF(69, "本人"),

        SPOUSE(70, "配偶"),

        PARENT(71, "父母"),

        CHILD(72, "子女"),

        RELATIVES(73, "亲属"),

        COLLEUAGUE(74, "同事"),

        FRIEND(75, "朋友"),

        OTHER(76, "其他"),

        UNIT(77, "单位"),

        STUDENT(219,"同学");

        private Integer value;

        private String remark;

        relation(Integer value, String remark) {
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
