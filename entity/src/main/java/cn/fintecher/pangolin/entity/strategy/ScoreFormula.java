package cn.fintecher.pangolin.entity.strategy;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Created by zzl029 on 2017/8/10.
 */
@Document
@Data
@ApiModel(value = "ScoreRule",
        description = "评分规则实体")
public class ScoreFormula {
    private String name;
    private String strategy;
    private String strategyJson;

    private BigDecimal score;
}
