package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : gaobeibei
 * @Description : APP新增客户地址信息
 * @Date : 16:01 2017/7/20
 */
@Data
public class AddressRepairInfo {
    @ApiModelProperty("地址类型")
    private Integer type; //地址类型
    @ApiModelProperty("地址状态")
    private Integer status; //地址状态
    @ApiModelProperty("详细地址")
    private String detail; //详细地址
}
