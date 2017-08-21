package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 修改状态参数
 * @Date : 16:16 2017/7/21
 */

@Data
public class PhoneStatusParams {
    private String personalAddressId; //联系人地址ID
    private String personalContactId; //联系人ID
    private Integer phoneStatus; //电话状态
    private Integer addressStatus; //地址状态
}