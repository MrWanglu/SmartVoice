package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "principal")
@ApiModel(value = "Principal",
        description = "委托方信息",
        parent = BaseEntity.class)
public class Principal extends BaseEntity {
    @ApiModelProperty("特定公司的标识")
    private String companyCode;
    @ApiModelProperty("委托方编码")
    private String code;
    @ApiModelProperty("委托方")
    private String name;
    @ApiModelProperty("市的id")
    private String area_id;
    @ApiModelProperty("详细地址")
    private String address;
    @ApiModelProperty("联系人")
    private String contacts;
    @ApiModelProperty("联系电话固话")
    private String phone;
    @ApiModelProperty("手机号")
    private String mobile;
    @ApiModelProperty("邮箱")
    private String email;
    @Size(max = 1000, message = "备注不能超过1000个字符")
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("创建时间")
    private Date operatorTime;
    @ManyToOne
    @JoinColumn(name = "operator")
    private User user;
    @ApiModelProperty("是否删除 0否1是")
    private Integer flag;
    @ApiModelProperty("机构类型")
    private Integer type;


}
