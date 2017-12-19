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
    @ApiModelProperty(notes = "特定公司的标识")
    private String companyCode;

    @ApiModelProperty(notes = "委托方编码")
    private String code;

    @ApiModelProperty(notes = "委托方")
    private String name;

    @ApiModelProperty(notes = "市的id")
    private Integer areaId;

    @ApiModelProperty(notes = "详细地址")
    private String address;

    @ApiModelProperty(notes = "联系人")
    private String contacts;

    @ApiModelProperty(notes = "联系电话固话")
    private String phone;

    @ApiModelProperty(notes = "手机号")
    private String mobile;

    @ApiModelProperty(notes = "邮箱")
    private String email;

    @Size(max = 1000, message = "备注不能超过1000个字符")
    @ApiModelProperty(notes = "备注")
    private String remark;

    @ApiModelProperty(notes = "创建时间")
    private Date operatorTime;

    @ManyToOne
    @JoinColumn(name = "operator")
    @ApiModelProperty(notes = "操作人")
    private User user;

    @ApiModelProperty(notes = "是否删除 0否1是")
    private Integer flag;

    @ApiModelProperty(notes = "机构类型")
    private Integer type;

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
