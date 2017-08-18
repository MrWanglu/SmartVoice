package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-04-19:51
 */
@Data
public class ObjectSon {
    String outs_name;
    String overdue_time;
    BigDecimal monrybili = new BigDecimal(0);
    BigDecimal money = new BigDecimal(0);
    BigDecimal hushubili = new BigDecimal(0);
    BigDecimal nummoney = new BigDecimal(0);
}
