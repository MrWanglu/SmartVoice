package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/2.
 */
@Data
public class CaseCountResult {
    private Integer status;

    private Integer num;

    private BigDecimal amt;

    public void initObject(){
        if(Objects.isNull(this.num)){
            this.num = 0;
        }
        if(Objects.isNull(this.amt)){
            this.amt = new BigDecimal(0.00);
        }
    }
}
