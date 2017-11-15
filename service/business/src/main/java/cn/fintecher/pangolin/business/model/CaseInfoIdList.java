package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-23-18:41
 */
@Data
@ApiModel(description = "回收案件司法参数")
public class CaseInfoIdList {
    @ApiModelProperty("选择的案件ID集合")
    List<String> ids;
    @ApiModelProperty(value = "司法说明", required = true)
    private String memo; // 司法说明
}
