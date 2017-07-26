package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @ManyToOne
    @JoinColumn(name = "personal_id")
    private Personal personalInfo;

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
}
