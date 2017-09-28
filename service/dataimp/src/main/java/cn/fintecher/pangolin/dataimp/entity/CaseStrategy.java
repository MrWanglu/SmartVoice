package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by luqiang on 2017/8/2.
 */
@Data
@Document
@ApiModel(value = "CaseStrategy",
        description = "案件分配策略")
public class CaseStrategy {
    @Id
    @ApiModelProperty("主键ID")
    private String id;
    @ApiModelProperty("策略名称")
    private String name;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建人ID")
    private String creatorId;
    @ApiModelProperty("策略指定的分配类型：0-机构，1-催收员，2-内催池待分配池，3-委外池带分配池，4-委外方")
    private Integer assignType;
    @ApiModelProperty("创建日期")
    private Date createTime;
    @ApiModelProperty("策略JSON对象")
    private String strategyJson;
    @ApiModelProperty("策略公式")
    private String strategyText;
    @ApiModelProperty("策略指定的催收员")
    private List<String> users;
    @ApiModelProperty("策略指定的机构")
    private List<String> departments;
    @ApiModelProperty("策略指定的委外方")
    private List<String> outsource;
    @ApiModelProperty("优先级")
    private Integer priority;
    @ApiModelProperty("公司Code")
    private String companyCode;
    @ApiModelProperty("策略类型：230-导入案件分配策略，231-内催池案件分配策略，232-委外池案件分配策略")
    private Integer strategyType;


    /**
     * 策略类型枚举
     */
    public enum StrategyType {
        IMPORT(230, "导入案件分配策略"),
        INNER(231, "内催池案件分配策略"),
        OUTS(232, "委外池案件分配策略");

        private Integer value;
        private String remark;

        StrategyType(Integer value, String remark) {
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
