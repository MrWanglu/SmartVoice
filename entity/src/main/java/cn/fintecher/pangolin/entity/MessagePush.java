package cn.fintecher.pangolin.entity;

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
    private String companyCode; //公司标识
    private String pushRegid; //推送标识
    private String deviceCode; //设备码
    private String deviceType; //设备类型
    private String systemVersion; //操作系统版本
    private Integer pushStatus; //推送接收的启用停用（0是启用 1是停用）
    private String operator; //操作人
    private Date operateTime; //操作时间
    private String field; //备用字段

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
