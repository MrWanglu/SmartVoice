package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by zzl029 on 2017/8/25.
 */
@Data
@ApiModel(value = "ScoreRule",
        description = "评分规则集合实体")
public class ScoreRules  {

    private List<ScoreRule> scoreRules;



}