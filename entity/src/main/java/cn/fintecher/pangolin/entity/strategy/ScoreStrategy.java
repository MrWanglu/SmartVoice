package cn.fintecher.pangolin.entity.strategy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by luqiang on 2017/8/10.
 */
@Data
@Document
@ApiModel(value = "ScoreStrategy",
        description = "评分规则实体")
public class ScoreStrategy {
    @ApiModelProperty(notes = "主键ID")
    private String id;
    @ApiModelProperty(notes = "是否启用")
    private int onOffFlag;
    @ApiModelProperty(notes = "规则名称")
    private String strategyName;
    @ApiModelProperty(notes = "原始字符串")
    private String jsonStr;
    @ApiModelProperty(notes = "规则集合")
    @DBRef
    private List<ScoreRule> sorceRuleList;
    @ApiModelProperty(notes = "创建人账号")
    private String createNo;
    @ApiModelProperty(notes = "创建人名称")
    private String createName;
    @ApiModelProperty(notes = "操作日期")
    private Date creatime;
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
