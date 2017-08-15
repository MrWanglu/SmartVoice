package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author : gaobeibei
 * @Description : 案件文件
 * @Date : 2017/8/15
 */
@Entity
@Table(name = "case_file")
@Data
public class CaseFile extends BaseEntity{
    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "文件ID")
    private String fileId;

    @ApiModelProperty(notes = "文件类型")
    private String fileType;

    @ApiModelProperty(notes = "文件地址")
    private String fileUrl;

    @ApiModelProperty("操作员")
    private String operator;

    @ApiModelProperty("操作时间")
    private Date operatorTime;

    @ApiModelProperty("公司Code")
    private String companyCode;

    @ApiModelProperty("案件ID")
    private String caseId;

    @ApiModelProperty("文件名称")
    private String fileName;
}
