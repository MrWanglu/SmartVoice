package cn.fintecher.pangolin.dataimp.entity;

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
}
