package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/26.
 */
@Data
public class ItemsModel {
    private List<String> personalItems;
    private List<String> jobItems;
    private List<String> connectItems;
    private List<String> caseItems;
    private List<String> bankItems;
    private List<String> followItems;
    private Integer category;
}
