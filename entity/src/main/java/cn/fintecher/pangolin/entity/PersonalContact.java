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
@Table(name = "personal_contact")
@Data
public class PersonalContact extends BaseEntity {
    private Integer relation;
    private String name;
    private Integer informed;
    private String phone;
    private Integer phoneStatus;
    private String mail;
    private String mobile;
    private String idCard;
    private String employer;
    private String department;
    private String position;
    private Integer source;
    private String address;
    private String workPhone;
    private String operator;
    private Date operatorTime;
    @ManyToOne
    @JoinColumn(name = "personal_id")
    private PersonalInfo personalInfo;

}
