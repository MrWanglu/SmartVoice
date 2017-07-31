package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "Personal")
@Data
public class Personal extends BaseEntity {
    private String name;
    private Integer sex; //0-男 1-女
    private Integer marital;
    private Integer education;
    private String school;
    private Integer age;
    private String mobileNo;
    private Integer mobileStatus;
    private String idCard;
    private String wechat;
    private String qq;
    private String email;
    private Integer idCardValidityPeriod;
    private String idCardIssuingAuthority; //身份证发证机关
    private String idCardAddress;
    private String cityLiveTime;
    private String localLiveTime;
    private String localPhoneNo;
    private String localHomeAddress;
    private String electricityAccount;
    private String referenceNo;
    private String referencePwd;
    private String referenceAuthCode;
    private String electricityPwd;
    private Integer dataSource;
    private String operator;
    private Date operatorTime;
    private String companyCode;

    @OneToMany(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "personalId", insertable = false, updatable = false)
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
    @OneToOne
    @JoinColumn(name = "personal_id")
    private PersonalJob personalJob; //客户工作信息
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
    public enum SexEnum{
        MAN(142,"男"),WOMEN(143,"女"),UNKNOWN(144,"未知");
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
    public enum RelationEnum{
        SELF(69,"本人"),PARTNER(70,"配偶"),PARENTS(71,"父母"),CHILDREN(72,"子女"),FAMILY(73,"亲属"),COLLEAGUE(74,"同事"),
        FRIENDS(75,"朋友"),OTHERS(76,"其他"),UNIT(77,"单位");
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

    public enum AddrRelationEnum{
        CURRENTADDR(83,"现居住地址"),UNITADDR(84,"单位地址"),IDCARDADDR(85,"身份证地址"),
        PROPERTYADDR(86,"房产地址"),OTHERS(87,"其他");
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
     * @Description 电话状态
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
}
