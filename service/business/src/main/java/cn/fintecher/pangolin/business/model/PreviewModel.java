package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaobeibei on 2017/11/1.
 */
@Data
public class PreviewModel {
    private List<CaseInfoInnerDistributeModel> list;
    private List<String> userOrDepartIds = new ArrayList<>();
    private List<String> caseIds = new ArrayList<>();
    private List<Integer> numList = new ArrayList<>();
    private List<OutDistributeInfo> OutList;
    private List<String> outsourceIds= new ArrayList<>();
}
