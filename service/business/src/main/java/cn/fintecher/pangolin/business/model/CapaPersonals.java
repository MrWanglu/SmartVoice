package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: gaobeibei
 * @Description: 智能催收客户信息
 * @Date 11:34 2017/9/1
 */
@Data
public class CapaPersonals {
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ApiModelProperty("客户ID")
    private String personalId;
    @ApiModelProperty("联系人ID集合")
    private List<String> concatIds;
    @ApiModelProperty("联系人姓名集合")
    private List<String> concatNames;
    @ApiModelProperty("联系人电话集合")
    private List<String> concatPhones;
    @ApiModelProperty("短信参数")
    private Map<String, String> params = new HashMap<>();
}
