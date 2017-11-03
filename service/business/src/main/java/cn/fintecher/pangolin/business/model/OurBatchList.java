package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by baizhangyu on 2017/6/8.
 */
@Data
public class OurBatchList {
    private List<String> ourBatchList;
    private List<String> outsNameList;
    private String outsName;
    private String outTimeStart;
    private String outTimeEnd;
    private String companyCode; //公司code码
}
