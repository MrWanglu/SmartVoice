package cn.fintecher.pangolin.common.model;

import lombok.Data;

import java.util.Map;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-21-15:34
 */
@Data
public class SmaErpv3Return {
    String sign;
    String reason;
    Map<String, String> map;
}
