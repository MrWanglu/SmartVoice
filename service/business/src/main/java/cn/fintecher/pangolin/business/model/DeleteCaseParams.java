package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/10/16.
 */
@Data
@ApiModel("案件删除传参")
public class DeleteCaseParams {
    @ApiModelProperty("要删除的案件ID集合")
    private List<String> ids;
}
