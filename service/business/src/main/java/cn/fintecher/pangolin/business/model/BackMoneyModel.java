package cn.fintecher.pangolin.business.model;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : gaobeibei
 * @Description : 回款详情临时对象for APP
 * @Date : 15:09 2017/7/31
 */
@Data
public class BackMoneyModel {
    private String  personalName; //客户姓名
    private Integer  collectionStatus; //催收状态
    private Integer payWay; //还款方式
    private Date createTime; //处理日期
    private BigDecimal payamt=new BigDecimal(0); //还款金额
    private Integer overdueDays; //逾期天数
    private String  paystatus; //还款状态
}