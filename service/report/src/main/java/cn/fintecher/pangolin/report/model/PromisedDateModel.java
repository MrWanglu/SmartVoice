package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/4.
 */
@Entity
@Data
public class PromisedDateModel {

    @ApiModelProperty(notes = "案件还款意向数据案件金额")
    private List<PromisedModel> totalAmtList;
    @ApiModelProperty(notes = "案件还款意向数据案件数量")
    private List<PromisedModel> totalCountList;
}
