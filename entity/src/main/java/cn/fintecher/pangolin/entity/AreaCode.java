package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "area_code")
@Data
public class AreaCode implements Serializable {
    @Id
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private AreaCode parent;
    private String treePath;
    private String areaCode;
    private String areaName;
    private String areaEnglishName;
    private String bankCode;
    private String zipCode;
    private String zoneCode;
    private String operator;
    private Date operatorTime;
}
