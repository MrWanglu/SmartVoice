package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "outsource")
@Data
public class OutsourceRecord extends BaseEntity {
    @ManyToOne
    @JoinColumn(name="case_id")
    private CaseInfo caseInfo;

    @ManyToOne
    @JoinColumn(name="outs_id")
    private Outsource outsource;

    @ApiModelProperty("案件编号")
    private String ouorOrdernum;

    @ApiModelProperty("批次")
    private String ouorBatch;

    @ApiModelProperty("委案日期")
    private Date ouorDate;

    @ApiModelProperty("备注")
    private String memo;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("操作人")
    private String creator;

    @ApiModelProperty("状态 0正常  1删除")
    private Integer flag;

}
