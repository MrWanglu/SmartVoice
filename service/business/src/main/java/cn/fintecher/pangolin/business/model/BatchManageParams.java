package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by huyanmin on 2017/9/19.
 */
@Data
public class BatchManageParams {
    //跑批状态
    private String sysStatus;
    //系统时间
    private Date sysDate;
}
