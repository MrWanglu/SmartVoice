package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "personal_bank")
@Data
public class PersonalBank extends BaseEntity {
    private String accountType;
    private String depositBank;
    private String depositBranch;
    private String cardNumber;
    private String depositProvince;
    private String depositCity;
    private String operator;
    private Date operatorTime;
    @ManyToOne
    @JoinColumn(name = "personal_id")
    private PersonalInfo personalInfo;

}
