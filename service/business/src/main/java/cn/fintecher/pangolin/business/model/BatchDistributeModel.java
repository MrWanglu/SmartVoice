package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 批量分配模型
 * @Date : 21:22 2017/7/20
 */

@Data
public class BatchDistributeModel {
    private Integer averageNum; //人均案件数
    private List<BatchInfoModel> batchInfoModelList; //案件分配信息模型集合
    private List<String> caseIds; //选择的分配案件ID集合
}
