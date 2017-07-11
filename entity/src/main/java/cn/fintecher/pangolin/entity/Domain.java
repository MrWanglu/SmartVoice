package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table
@Data
public class Domain extends BaseEntity {
    private String name;

}
