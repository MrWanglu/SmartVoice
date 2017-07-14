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
@Table(name = "personal_address")
@Data
public class PersonalAddress extends BaseEntity {
    private Integer relation;
    private String name;
    private Integer type;
    private Integer status;
    private String detail;
    private Integer source;
    private String operator;
    private Date operatorTime;
    @ManyToOne
    @JoinColumn(name = "personal_id")
    private Personal personalInfo;

}
