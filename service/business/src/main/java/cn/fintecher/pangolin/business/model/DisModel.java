package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.CaseInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */
@Data
public class DisModel {
    private String id;
    private BigDecimal amt = new BigDecimal(0);
    private List<String> caseIds = new ArrayList<>();
    private List<CaseInfo> caseInfos = new ArrayList<>();
    private int num ;
}
