package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description: 短信群发页面参数
 * Date: 2017-07-06-13:43
 */
@Data
public class MessageBatchSendRequest {
    private String custId; //客户ID
    private String caseNumber; //案件编号
    private String custName; //客户姓名
    private List<Integer> relation; //客户关系人列表
    private List<String> phone; //客户关系人手机号
    private List<Integer> status; //状态 0：未知 1：正常
    private List<String> nameList; //关系人姓名\
    private List<String> concatIds; //关系人ID
}
