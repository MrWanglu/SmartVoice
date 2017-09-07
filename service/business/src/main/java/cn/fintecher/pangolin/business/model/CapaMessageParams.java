package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: gaobeibei
 * @Description: 智能催收短信参数
 * @Date 11:34 2017/9/1
 */
@Data
public class CapaMessageParams {
    @ApiModelProperty("客户列表")
    List<CapaPersonals> capaPersonals;
    @ApiModelProperty("发送类型")
    String sendType;
    @ApiModelProperty("模板ID")
    String tesmId;
}
