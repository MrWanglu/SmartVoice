package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
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
    private Integer source;
    private String detail;
    private String personalId;
    private String operator;
    private Date operatorTime;
    private BigDecimal longitude;
    private BigDecimal latitude;
}

