package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : gaobeibei
 * @Description : APP新增客户信息
 * @Date : 16:01 2017/7/20
 */
@Data
public class PersonalRepairInfo {
    @ApiModelProperty("客户信息ID")
    private String personalId; //客户信息ID
    @ApiModelProperty("关系")
    private Integer relation; //关系
    @ApiModelProperty("姓名")
    private String name; //姓名
    @ApiModelProperty("电话信息")
    private List<PhoneRepairInfo> phoneList = new ArrayList<>(); //电话信息
    @ApiModelProperty("地址信息")
    private List<AddressRepairInfo> addressList = new ArrayList<>(); //地址信息
    @ApiModelProperty("社交账号")
    private List<SocialRepairInfo> socialList = new ArrayList<>(); //社交账号
    @ApiModelProperty("文件集合")
    private List<String> fileIds; //文件集合
    @ApiModelProperty("修复说明")
    private String remark; //修复说明
    @ApiModelProperty("案件ID")
    private String caseId; //案件ID
    @ApiModelProperty("案件编号")
    private String caseNnumber; //案件编号
}
