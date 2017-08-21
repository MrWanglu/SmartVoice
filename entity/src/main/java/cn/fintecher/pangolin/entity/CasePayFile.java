package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "case_pay_file")
@Data
public class CasePayFile extends BaseEntity {
    private String payId;
    private String caseNumber;
    private String fileid;
    private String operator;
    private String operatorName;
    private Date operatorTime;
    private String caseId;
}
