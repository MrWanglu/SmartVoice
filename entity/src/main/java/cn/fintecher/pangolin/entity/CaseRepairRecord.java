package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseRepairRecord.java,v 0.1 2017/8/8 16:26 yuanyanting Exp $$
 */

@Entity
@Data
@Table(name = "case_repair_record")
public class CaseRepairRecord extends BaseEntity{

    @ApiModelProperty(notes = "修复文件id")
    private String id;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "文件ID")
    private String fileId;

    @ApiModelProperty(notes = "文件类型")
    private String fileType;

    @ApiModelProperty(notes = "文件地址")
    private String fileUrl;

    @ApiModelProperty("修复说明")
    private String repairMemo;

    @ApiModelProperty("操作员")
    private String operator;

    @ApiModelProperty("操作时间")
    private Date operatorTime;


    private String caseId;


}
