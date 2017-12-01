package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(notes = "跟进记录ID")
    private CaseFollowupRecord followupId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "文件ID")
    private String fileid;

    @ApiModelProperty(notes = "文件类型")
    private String filetype;

    @ApiModelProperty(notes = "文件地址")
    private String fileurl;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建人名称")
    private String operatorName;

    @ApiModelProperty(notes = "创建时间")
    private Date operatorTime;

}
