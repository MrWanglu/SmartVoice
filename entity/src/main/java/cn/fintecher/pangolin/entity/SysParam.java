package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author : sunyanping
 * @Description : 系统参数表
 * @Date : 2017/7/18.
 */
@Data
@Table(name = "sys_param")
@Entity
@ApiModel(value = "SysParam", description = "系统参数")
public class SysParam extends BaseEntity {
    @ApiModelProperty("公司的特定标识")
    private String companyCode;
    @ApiModelProperty("参数的自定义code")
    private String code;
    @ApiModelProperty("参数名称")
    private String name;
    @ApiModelProperty("参数是否启用（0是启用 1 是停用）")
    private Integer status;
    @ApiModelProperty("参数类型（服务的端口号）")
    private String type;
    @ApiModelProperty("参数值")
    private String value;
    @ApiModelProperty("标识（0是可以修改 1是不能修改）")
    private Integer sign;
    @ApiModelProperty("创建人")
    private String operator;
    @ApiModelProperty("创建时间")
    private Date operateTime;
    @ApiModelProperty("参数说明")
    private String remark;
    @ApiModelProperty("备用字段")
    private String field;

    /**
     * 参数启用停用
     */
    public enum StatusEnum{
        Start(0,"启用"),Stop(1,"停用");
        Integer value;
        String  code;
        StatusEnum(Integer value,String code){
            this.value=value;
            this.code=code;
        }

        public Integer getValue() {
            return value;
        }
        public String getCode() {
            return code;
        }
    }
}
