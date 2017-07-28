package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "outsource")
@Data
public class OutsourcePool extends BaseEntity {
    @ManyToOne
    @JoinColumn(name="case_id")
    private CaseInfo caseInfo;

    @ManyToOne
    @JoinColumn(name="outs_id")
    private Outsource outsource;

    @ApiModelProperty("委外时间")
    private Date outTime;

    @ApiModelProperty("操作时间")
    private Date operateTime;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("委外状态")
    private Integer outStatus;

    @ApiModelProperty("委外批次号")
    private String outBatch;

    public enum OutStatus {

        //待委外
        TO_OUTSIDE(167),
        //委外中
        OUTSIDING(168),
        //委外到期
        OUTSIDE_EXPIRE(169),
        //委外结束
        OUTSIDE_OVER(170);
        private Integer code;

        OutStatus(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }
}