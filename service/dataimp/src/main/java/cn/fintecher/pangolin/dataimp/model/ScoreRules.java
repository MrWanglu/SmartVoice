package cn.fintecher.pangolin.dataimp.model;

import cn.fintecher.pangolin.dataimp.entity.ScoreRule;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by zzl029 on 2017/8/22.
 */
@Data
@ApiModel(value = "ScoreRule",
        description = "评分规则集合实体")
public class ScoreRules {
    private List<ScoreRule> scoreRules;
}
