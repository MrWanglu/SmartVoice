package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "outsource_record")
@Data
public class OutsourceRecord extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "case_id")
    @ApiModelProperty(notes = "案件ID")
    private CaseInfo caseInfo;

    @ManyToOne
    @JoinColumn(name = "outs_id")
    private Outsource outsource;

    @ApiModelProperty(notes = "案件编号")
    private String ouorOrdernum;

    @ApiModelProperty(notes = "批次")
    private String ouorBatch;

    @ApiModelProperty(notes = "委案日期")
    private Date ouorDate;

    @ApiModelProperty(notes = "备注")
    private String memo;

    @ApiModelProperty(notes = "创建时间")
    private Date createTime;

    @ApiModelProperty(notes = "操作人")
    private String creator;

    @ApiModelProperty(notes = "状态 0正常  1删除")
    private Integer flag;

    @ApiModelProperty(notes = "佣金回款 204 回款 205 回退 206 修复")
    private Integer operationType;
}
