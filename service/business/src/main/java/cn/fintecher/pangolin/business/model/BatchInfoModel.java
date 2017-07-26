package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.User;
import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 案件分配信息模型
 * @Date : 17:25 2017/7/20
 */

@Data
public class BatchInfoModel {
    private Integer caseCount; //持有案件数
    private User collectionUser; //催收人
    private Integer distributionCount;//分配数量
}