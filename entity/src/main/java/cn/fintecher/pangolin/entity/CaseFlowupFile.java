package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "case_flowup_file")
@Data
public class CaseFlowupFile extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "followup_id")
    private CaseFollowupRecord followupId;
    private String caseNumber;
    private String caseId;
    private String fileid;
    private String filetype;
    private String fileurl;
    private String operator;
    private String operatorName;
    private Date operatorTime;

}
