package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "personal_contact")
@Data
public class PersonalContact extends BaseEntity {
    private Integer relation;
    private String name;
    private Integer informed;
    private String phone;
    private Integer phoneStatus;
    private String mail;
    private String mobile;
    private String idCard;
    private String employer;
    private String department;
    private String position;
    private Integer source;
    private String address;
    private String workPhone;
    private String operator;
    private Date operatorTime;
    private Integer socialType; //社交帐号类型
    private String socialValue; //社交帐号内容
    private String personalId; //客户信息ID
    private Integer addressStatus; //地址状态

    /**
     * @Description 社交帐号枚举类
     */
    public enum SocialType {
        //微信
        WECHAT(159, "微信"),
        //QQ
        QQ(160, "QQ"),
        //其他
        OTHER(161, "其他");

        private Integer value;

        private String remark;

        SocialType(Integer value, String remark) {
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
     * @Description 客户关系枚举类
     */
    public enum relation {

        SELF(69, "本人"),

        SPOUSE(70, "配偶"),

        PARENT(71, "父母"),

        CHILD(72, "子女"),

        RELATIVES(73, "亲属"),

        COLLEUAGUE(74, "同事"),

        FRIEND(75, "朋友"),

        OTHER(76, "其他"),

        UNIT(77, "单位");

        private Integer value;

        private String remark;

        relation(Integer value, String remark) {
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
