package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by  gaobeibei.
 * Description: 地图模型
 * Date: 2017-08-18
 */
@Data
public class MapModel {
    private String address;
    private double longitude;
    private double latitude;
}
