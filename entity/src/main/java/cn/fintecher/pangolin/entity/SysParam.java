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
    @ApiModelProperty(notes = "公司的特定标识")
    private String companyCode;

    @ApiModelProperty(notes = "参数的自定义code")
    private String code;

    @ApiModelProperty(notes = "参数名称")
    private String name;

    @ApiModelProperty(notes = "参数是否启用（0是启用 1 是停用）")
    private Integer status;

    @ApiModelProperty(notes = "参数类型（服务的端口号）")
    private String type;

    @ApiModelProperty(notes = "参数值")
    private String value;

    @ApiModelProperty(notes = "标识（0是可以修改 1是不能修改）")
    private Integer sign;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建时间")
    private Date operateTime;

    @ApiModelProperty(notes = "参数说明")
    private String remark;

    @ApiModelProperty(notes = "备用字段")
    private String field;

    @ApiModelProperty(notes = "参数类型")
    private Integer style;

    /**
     * 参数启用停用
     */
    public enum StatusEnum {
        Start(0, "启用"), Stop(1, "停用");
        Integer value;
        String code;

        StatusEnum(Integer value, String code) {
            this.value = value;
            this.code = code;
        }

        public Integer getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * @Descritpion 参数类型枚举类
     */
    public enum Style {
        //流转参数
        CIRCULATION(0, "流转参数"),
        //批量参数
        BATCH(1, "批量参数"),
        //模版参数
        TEMPLATE(2, "模版参数"),
        //呼叫中心参数
        CALL(3, "呼叫中心参数"),
        //短信发送参数
        SMS(4, "短信参数"),
        //其他参数
        OTHER(5, "其他参数");

        private Integer value;

        private String remark;

        Style(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }
}
