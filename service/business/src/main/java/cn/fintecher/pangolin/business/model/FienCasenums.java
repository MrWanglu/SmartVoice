package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
@ApiModel(value = "FienCasenums", description = "财务数据信息id集合")
public class FienCasenums {
    @ApiModelProperty("财务数据id")
    private List<String> idList;
}
