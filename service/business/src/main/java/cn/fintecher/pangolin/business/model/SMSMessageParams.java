package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送短息参数
 * @Author: PeiShouWen
 * @Description:
 * @Date 11:31 2017/9/1
 */
@Data
public class SMSMessageParams {

    //客户id
    private String personalId;
    //客户信息（电话，姓名）
    private List<PersonalParams> personalParamsList;
    //短信编码
    private String tesmId;
    //发送方式 (161 自动，162 手动)
    private Integer sendType;
    //客户类型 0 客户修复信息 1 客户联系人信息
    private String custType;
    //客户联系人ID（原始联系人ID）
    private String contId;
    //案件编码
    private String caseNumber;
    //短信参数
    private Map<String, String> params = new HashMap<>();

    //发送方式
    public enum SendType {
        //自动
        Auto(108),
        //手动
        Handle(109);

        private Integer value;

        SendType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }
}
