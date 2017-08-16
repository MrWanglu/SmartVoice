package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-16-14:21
 */
@Data
public class OutSourceCommissionIds {
    @ApiModelProperty(notes = "委外方佣金id集合")
    private List<String> ids;
}
