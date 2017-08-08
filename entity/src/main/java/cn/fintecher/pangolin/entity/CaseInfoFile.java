package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 14:11 2017/7/24
 */
@Data
@Entity
@Table(name = "case_file")
public class CaseInfoFile extends BaseEntity {

    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "文件ID")
    private String fileId;

    @ApiModelProperty(notes = "文件名称")
    private String fileName;

    @ApiModelProperty(notes = "文件路径")
    private String fileUrl;

    @ApiModelProperty(notes = "文件类型")
    private String fileType;

    @ApiModelProperty("创建时间")
    private Date operatorTime;

    @ApiModelProperty("操作人员")
    private String operator;

    @ApiModelProperty("公司码")
    private String companyCode;
}
