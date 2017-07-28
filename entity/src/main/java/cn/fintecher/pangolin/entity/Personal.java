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
    private Integer sex;
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
    private String electricityPwd;
    private Integer dataSource;
    private String operator;
    private Date operatorTime;
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
}
