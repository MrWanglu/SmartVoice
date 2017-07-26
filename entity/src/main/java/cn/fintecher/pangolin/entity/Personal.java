package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String idCardAddress;
    private String cityLiveTime;
    private String localLiveTime;
    private String cityHouseStatus;
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
    @JsonIgnore
    @OneToMany(mappedBy = "personalInfo", targetEntity = PersonalContact.class)
    private Set<PersonalContact> personalContacts;
    @JsonIgnore
    @OneToMany(mappedBy = "personalInfo", targetEntity = PersonalBank.class)
    private Set<PersonalBank> personalBankInfos;
    @OneToMany(cascade= {CascadeType.ALL},fetch=FetchType.LAZY ,mappedBy = "personalId", targetEntity = PersonalAddress.class)
    private Set<PersonalAddress> personalAddresses;
}
