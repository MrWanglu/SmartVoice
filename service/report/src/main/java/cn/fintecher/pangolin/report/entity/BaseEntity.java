package cn.fintecher.pangolin.report.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 18:15 2017/8/1
 */
@Data
public class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID",strategy = GenerationType.AUTO)
    private String id;

    @Transient
    private Integer page = 1;

    @Transient
    private Integer rows = 10;
}
