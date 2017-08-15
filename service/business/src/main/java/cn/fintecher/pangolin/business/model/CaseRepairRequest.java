package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author yuanyanting
 * @version Id:CaseRepairRequest.java,v 0.1 2017/8/8 16:07 yuanyanting Exp $$
 * 案件修复的文件参数
 */

@Data
public class CaseRepairRequest {

    /** 文件的id集合 */
    private List<String> fileIds;

    /** 修复说明 */
    private String repairMemo;

    private String id;

}
