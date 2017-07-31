package cn.fintecher.pangolin.business.utils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 数字转化工具类
 * @Date 17:33 2017/7/28
 */
public class ZWMathUtil {

    /**
     * Double 转化为 BigDecimal
     * @param value 值
     * @param scale 精读
     * @param roundingMode 四舍五入方式
     * @return
     */
    public static BigDecimal DoubleToBigDecimal(Double value ,Integer scale, Integer roundingMode){
        if(Objects.nonNull(scale) && Objects.nonNull(roundingMode)){
            return new BigDecimal(value).setScale(scale,roundingMode);
        }else if(Objects.nonNull(scale) && Objects.isNull(roundingMode)){
            return new BigDecimal(value).setScale(scale,BigDecimal.ROUND_HALF_UP);
        }else if(Objects.nonNull(roundingMode) && Objects.isNull(scale)){
            return new BigDecimal(value).setScale(2,roundingMode);
        }else{
            return new BigDecimal(value).setScale(2,BigDecimal.ROUND_HALF_UP);
        }

    }
}
