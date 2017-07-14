package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/10.
 */

@Entity
@Table
@Data
public class Role extends BaseEntity {
    private String companyCode;
    private String name;
    private Integer type;
    private Integer status;
    private String remark;
    private String operator;
    private Date operateTime;

}
