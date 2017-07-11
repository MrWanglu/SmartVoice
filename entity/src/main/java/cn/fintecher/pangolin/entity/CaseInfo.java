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
public class CaseInfo extends BaseEntity {

    private PersonalInfo personalInfo;
    private Operator operator;
    private Domain domain;
    private Department department;
}
