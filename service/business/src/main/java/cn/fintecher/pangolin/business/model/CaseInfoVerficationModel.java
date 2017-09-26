package cn.fintecher.pangolin.business.model;

import lombok.Data;
import java.util.List;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerficationModel.java,v 0.1 2017/9/20 11:55 yuanyanting Exp $$
 * 核销案件的批量导出
 */
@Data
public class CaseInfoVerficationModel {

    private List<String> ids; // 核销案件的id集合

    private String companyCode; // 公司code

    private String state; // 打包说明

    private String id; // 核销案件的id

    private String applicationReason; // 申请原因

    private String approvalOpinion; //审批意见

    private Integer approvalResult; // 审批结果

}
