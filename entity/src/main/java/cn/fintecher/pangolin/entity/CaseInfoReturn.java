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

    @ApiModelProperty(notes = "回收说明")
    private String reason;

    @ApiModelProperty(notes = "退回来源 内催，委外，司法，核销")
    private Integer source;

    @ApiModelProperty("委外方名称")
    private String outsName;

    @ApiModelProperty("委外时间")
    private Date outTime;

    @ApiModelProperty("委外结案日期")
    private Date overOutsourceTime;

    @ApiModelProperty("委外批次号")
    private String outBatch;

    @ApiModelProperty("公司Code")
    private String companyCode;

    /**
     * 案件退回来源
     */
    public enum Source {
        //内催
        INTERNALCOLLECTION(225,"内催"),
        //委外
        OUTSOURCE(226, "委外"),
        //司法
        JUDICATURE(227, "司法"),
        //核销
        VERIFICATION(228, "核销");

        private Integer value;

        private String remark;

        Source(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }

}
