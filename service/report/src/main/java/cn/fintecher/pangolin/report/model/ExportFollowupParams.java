package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by Administrator on 2017/8/22.
 */
@Data
public class ExportFollowupParams {
    private String caseNum;
    private String batchNum;
    private String principalName;
    private Date followTime;
    private Integer followType;
    private String personName;
    private String idCard;
    private Integer target;
    private String targetName;
    private String contactPhone;
    private String detail;
    private String collectionLocation;
    private Integer collectionFeedback;
    private String content;
    private String personNumber;
    private String accountNumber;
    private Integer handNumber;
}
