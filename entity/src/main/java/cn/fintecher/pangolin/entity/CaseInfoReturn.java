package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 案件退案池
 * @Date : 16:52 2017/9/19
 */
@Entity
@Table(name = "case_info_return")
@Data
public class CaseInfoReturn extends BaseEntity {
    @OneToOne
    @ApiModelProperty(notes = "案件ID")
    @JoinColumn(name = "case_id")
    private CaseInfo caseId;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "操作人用户名")
    private String operator;

    @ApiModelProperty(notes = "退案原因")
    private String reason;
}
