package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/26.
 */
@Data
public class AssistCaseBatchModel {
    private Integer averageNum; //人均案件数
    private List<BatchInfoModel> batchInfoModelList; //案件分配信息模型集合
    private List<String> assistIds; //选择的分配的协催案件ID集合
}
