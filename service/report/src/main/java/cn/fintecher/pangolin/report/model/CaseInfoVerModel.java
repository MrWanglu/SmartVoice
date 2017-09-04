package cn.fintecher.pangolin.report.model;

import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerModel.java,v 0.1 2017/9/1 13:56 yuanyanting Exp $$
 */

@Data
@Entity
public class CaseInfoVerModel {

    private Integer cityCount; // 累计户数

    private String city; // 城市

    private String amount; // 累计回款金额
}
