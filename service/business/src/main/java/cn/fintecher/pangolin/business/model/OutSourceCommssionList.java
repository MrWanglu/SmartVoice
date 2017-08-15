package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.OutSourceCommssion;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-15-13:35
 */
@Data
public class OutSourceCommssionList {
    @ApiModelProperty(notes = "委外方佣金集合")
    private List<OutSourceCommssion> outsourceCommissionList;
}
