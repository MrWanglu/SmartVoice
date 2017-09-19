package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 修改备注参数
 * @Date : 10:54 2017/9/19
 */

@Data
public class ModifyMemoParams {
    private String caseId; //案件ID
    private String memo; //备注信息
}