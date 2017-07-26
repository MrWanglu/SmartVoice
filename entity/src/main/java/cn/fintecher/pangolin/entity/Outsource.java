package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "outsource")
@Data
public class Outsource extends BaseEntity {
    @ApiModelProperty("委外方编码")
    private String outsCode;

    @ApiModelProperty("委外方")
    private String outsName;

    @ApiModelProperty("省份ID")
    private Integer outsPrid;

    @ApiModelProperty("城市ID")
    private Integer outsCityid;

    @ApiModelProperty("区县ID")
    private Integer outsCtyid;

    @ApiModelProperty("省份")
    private String outsProvinces;

    @ApiModelProperty("城市")
    private String outsCity;

    @ApiModelProperty("区县")
    private String outsCounty;

    @ApiModelProperty("详细地址")
    private String outsAddress;

    @ApiModelProperty("联系人")
    private String outsContacts;

    @ApiModelProperty("联系电话")
    private String outsPhone;

    @ApiModelProperty("手机号")
    private String outsMobile;

    @ApiModelProperty("邮箱")
    private String outsEmail;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    @ApiModelProperty("备注")
    private String outsRemark;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("操作人")
    private String creator;

    @ApiModelProperty("是否删除 0否1是")
    private Integer flag;

    @ApiModelProperty("机构类型")
    private Integer outsOrgtype;

    //委托方管理枚举
    public enum principalStatus {
        //委托方编码位数       机构类型
        PRINCODE_DIGIT(3), ORGANIZATION(0014);
        private Integer principalCode;

        principalStatus(Integer principalCode) {
            this.principalCode = principalCode;
        }

        public Integer getPrincipalCode() {
            return principalCode;
        }
    }

    //委托方删除状态
    public enum deleteStatus {
        //启用    停用
        START(0), BLOCK(1);
        private Integer deleteCode;

        deleteStatus(Integer deleteCode) {
            this.deleteCode = deleteCode;
        }

        public Integer getDeleteCode() {
            return deleteCode;
        }
    }
}
