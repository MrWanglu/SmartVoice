package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author : gaobeibei
 * @Description : APP首页信息展示模型
 * @Date : 16:01 2017/7/24
 */
@Data
public class UserStatisAppModel {
    private BigDecimal commissionAmt = new BigDecimal(0); //佣金
    private BigDecimal applyPayAmt = new BigDecimal(0); //待审批回款金额
    private BigDecimal collectionAmt = new BigDecimal(0); //待催收回款金额
    private Integer weekVisitNum; //周外访数
    private Integer monthVisitNum;  //月外访数
    private Integer weekAssistNum;  //周协催数
    private Integer monthAssistNum; //月协催数
    private Integer weekCollectionNum; //周催计数
    private Integer monthCollectionNum; //月催计数
    private List<RankModel> payList; //周回款榜
    private List<RankModel> followList; //周跟催榜
    private List<RankModel> collectionList; //周催计榜

}
