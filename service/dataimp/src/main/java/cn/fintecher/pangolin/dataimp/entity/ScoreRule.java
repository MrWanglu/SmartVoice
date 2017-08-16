package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.List;

/**
 * Created by zzl029 on 2017/8/10.
 */
@Document
@Data
@ApiModel(value = "ScoreRule",
        description = "评分规则实体")
public class ScoreRule {
    @ApiModelProperty(notes = "主键ID")
    @Id
    private String id;
    @ApiModelProperty(notes = "属性名称")
    private String name;
    @ApiModelProperty(notes = "权重")
    private Double weight;
    @ApiModelProperty(notes = "公式与分值集合")
    private List<ScoreFormula> formulas;
}
