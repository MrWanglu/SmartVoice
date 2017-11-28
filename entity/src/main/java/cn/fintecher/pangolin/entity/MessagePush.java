package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author : gaobeibei
 * @Description : APP消息服务注册实体
 * @Date : 11:15 2017/8/1
 */
@Entity
@Table(name = "message_push")
@Data
public class MessagePush extends BaseEntity{
    @ApiModelProperty(notes = "公司标识")
    private String companyCode;

    @ApiModelProperty(notes = "推送标识")
    private String pushRegid;

    @ApiModelProperty(notes = "设备码")
    private String deviceCode;

    @ApiModelProperty(notes = "设备类型")
    private String deviceType;

    @ApiModelProperty(notes = "操作系统版本")
    private String systemVersion;

    @ApiModelProperty(notes = "推送接收的启用停用（0是启用 1是停用）")
    private Integer pushStatus;

    @ApiModelProperty(notes = "操作人")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operateTime;

    @ApiModelProperty(notes = "备用字段")
    private String field;

    public enum PushStatus {
        Enable(0,"启用"), Disable(1,"停用");
        private Integer value;
        private String remark;

        PushStatus(Integer value, String remark) {
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
