package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by baizhangyu on 2017/7/26
 */
@Data
public class OutsourceInfo {

    String outsId;//委外方Id
    private List<String> caseIds;//案件id集合
}
