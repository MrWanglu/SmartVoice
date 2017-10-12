package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by baizhangyu on 2017/6/8.
 */
@Data
public class OurBatchList {
    private List<String> ourBatchList;
    private String companyCode; //公司code码
}
