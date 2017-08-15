package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : gaobeibei
 * @Description : APP新增客户电话信息
 * @Date : 16:01 2017/7/20
 */
@Data
public class PhoneRepairInfo {
    @ApiModelProperty("电话状态")
    private Integer phoneStatus; //电话状态
    @ApiModelProperty("电话号码")
    private String phone; //电话号码
}
