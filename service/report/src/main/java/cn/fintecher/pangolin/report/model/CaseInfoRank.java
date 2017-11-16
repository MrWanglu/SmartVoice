package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/7.
 */
@Entity
@Data
public class CaseInfoRank {

    // 第六部分 催收计数排名
    private List<FollowCountModel> followCountModels;
    // 第五部分 催收员回款排名
    private List<BackAmtModel> backAmtModels;
    //催收员排名
    private Integer collectRank;
}
