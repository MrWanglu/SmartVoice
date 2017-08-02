package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "case_flowup_file")
@Data
public class CaseFlowupFile extends BaseEntity {
    private String followupId;
    private String caseNumber;
    private String fileid;
    private String filetype;
    private String fileurl;
    private String operator;
    private String operatorName;
    private Date operatorTime;

}
