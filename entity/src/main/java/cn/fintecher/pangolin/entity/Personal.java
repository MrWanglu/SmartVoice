package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "Personal")
@Data
public class Personal extends BaseEntity {
    @ApiModelProperty(notes = "姓名")
    private String name;

    @ApiModelProperty(notes = "性别")
    private Integer sex;

    @ApiModelProperty(notes = "婚姻状态")
    private Integer marital;

    @ApiModelProperty(notes = "教育程度")
    private Integer education;

    @ApiModelProperty(notes = "毕业学校")
    private String school;

    @ApiModelProperty(notes = "年龄")
    private Integer age;

    @ApiModelProperty(notes = "手机号码")
    private String mobileNo;

    @ApiModelProperty(notes = "手机号码状态")
    private Integer mobileStatus;

    @ApiModelProperty(notes = "身份证号码")
    private String idCard;

    @ApiModelProperty(notes = "微信号")
    private String wechat;

    @ApiModelProperty(notes = "QQ号")
    private String qq;

    @ApiModelProperty(notes = "电子邮箱地址")
    private String email;

    @ApiModelProperty(notes = "身份证有效期")
    private Integer idCardValidityPeriod;

    @ApiModelProperty(notes = "身份证发证机关")
    private String idCardIssuingAuthority;

    @ApiModelProperty(notes = "身份证地址")
    private String idCardAddress;

    @ApiModelProperty(notes = "本市生活时长")
    private String cityLiveTime;

    @ApiModelProperty(notes = "现居生活时长")
    private String localLiveTime;

    @ApiModelProperty(notes = "居住地家庭座机")
    private String localPhoneNo;

    @ApiModelProperty(notes = "现居住地址")
    private String localHomeAddress;

    @ApiModelProperty(notes = "电费户名")
    private String electricityAccount;

    @ApiModelProperty(notes = "电费密码")
    private String electricityPwd;

    @ApiModelProperty(notes = "产品内外部标识0-Excel,1-接口同步")
    private Integer dataSource;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "经度")
    private BigDecimal longitude;

    @ApiModelProperty(notes = "纬度")
    private BigDecimal latitude;

    @ApiModelProperty(notes = "客户号")
    private String number;

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    @OrderBy("relation asc")
    private Set<PersonalContact> personalContacts; //客户联系人

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalBank> personalBankInfos; //客户开户信息

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalAddress> personalAddresses; //客户地址信息

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalCar> personalCars; //客户车产信息

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalIncomeExp> personalIncomeExps; //客户收支信息

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalJob> personalJobs; //客户工作信息

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
    private Set<PersonalProperty> personalProperties; //客户房产信息

    /**
     * @Description 电话状态
     */
    public enum PhoneStatus {
        NORMAL(64, "正常"),
        VACANT_NUMBER(65, "空号"),
        HALT(66, "停机"),
        //提前结清还款中
        POWER_OFF(67, "关机"),
        UNKNOWN(68, "未知");
        private Integer value;
        private String remark;

        PhoneStatus(Integer value, String remark) {
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
     * 性别
     */
    public enum SexEnum {
        MAN(142, "男"), WOMEN(143, "女"), UNKNOWN(144, "未知");
        private Integer value;
        private String remark;

        SexEnum(Integer value, String remark) {
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
     * 关系
     */
    public enum RelationEnum {
        SELF(69, "本人"), PARTNER(70, "配偶"), PARENTS(71, "父母"), CHILDREN(72, "子女"), FAMILY(73, "亲属"), COLLEAGUE(74, "同事"),
        FRIENDS(75, "朋友"), OTHERS(76, "其他"), UNIT(77, "单位"), STUDENT(219, "同学");
        private Integer value;
        private String remark;

        RelationEnum(Integer value, String remark) {
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

    public enum AddrRelationEnum {
        CURRENTADDR(83, "现居住地址"), UNITADDR(84, "单位地址"), IDCARDADDR(85, "身份证地址"),
        PROPERTYADDR(86, "房产地址"), OTHERS(87, "其他");
        private Integer value;
        private String remark;

        AddrRelationEnum(Integer value, String remark) {
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
     * @Description 地址状态
     */
    public enum AddrStatus {
        VALIDADDR(148, "有效地址"),
        NOADDR(149, "地址不存在"),
        UNRELATEDADDR(150, "无关地址"),
        SALEOFF(151, "已变卖"),
        RENTOUT(152, "出租"),
        VACANCY(153, "空置"),
        UNKNOWN(154, "未知");
        private Integer value;
        private String remark;

        AddrStatus(Integer value, String remark) {
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

    public enum MARITAL {
        UNMARRIED(207, "未婚"),
        MARRIED(208, "已婚"),
        UNKNOW(209, "未知");
        private Integer value;
        private String remark;

        MARITAL(Integer value, String remark) {
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
