package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 发送信息记录实体
 * @Date : 14:20 2017/7/20
 */

@Entity
@Table(name = "send_message_record")
@Data
public class SendMessageRecord extends BaseEntity {
    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "客户信息ID")
    private String personalId;

    @ApiModelProperty(notes = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "客户联系人ID")
    private String personalContactId;

    @ApiModelProperty(notes = "发送对象")
    private Integer target;

    @ApiModelProperty(notes = "发送对象姓名")
    private String targetName;

    @ApiModelProperty(notes = "发送成功标识")
    private Integer flag;

    @ApiModelProperty(notes = "发送方式")
    private Integer sendWay;

    @ApiModelProperty(notes = "操作人")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人姓名")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorDate;

    @ApiModelProperty(notes = "模版ID")
    private String tempelateId;

    @ApiModelProperty(notes = "模版名称")
    private String tempelateName;

    @ApiModelProperty(notes = "模版编码")
    private String tempelateCode;

    @ApiModelProperty(notes = "模版类别")
    private Integer tempelateType;

    @ApiModelProperty(notes = "发送信息类别")
    private Integer messageType;

    @ApiModelProperty(notes = "发送内容")
    private String messageContent;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    /**
     * @Description 发送信息类别枚举类
     */
    public enum MessageType {
        //短信
        SMS(101, "短信"),
        //语音
        VOICE(102, "语音"),
        //电子邮件
        EMAIL(103, "电子邮件"),
        //信函
        LETTER(104, "信函"),
        //电催话术
        TEL_SURGERY(105, "电催话术");

        private Integer value;

        private String remark;

        MessageType(Integer value, String remark) {
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

    /**
     * @Description 模版类别枚举类
     */
    public enum TempelateType {
        //提醒
        REMIND(106, "提醒"),
        //催收
        COLLECTION(107, "催收");

        private Integer value;

        private String remark;

        TempelateType(Integer value, String remark) {
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

    /**
     * @Description 发送方式枚举类
     */
    public enum SendWay {
        //自动
        AUTOMATIC(108, "自动"),
        //手动
        MANUAL(109, "手动");

        private Integer value;

        private String remark;

        SendWay(Integer value, String remark) {
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

    /**
     * @Description 发送结果枚举类
     */
    public enum Flag {
        //自动
        AUTOMATIC(0, "成功"),
        //手动
        MANUAL(1, "失败");

        private Integer value;

        private String remark;

        Flag(Integer value, String remark) {
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