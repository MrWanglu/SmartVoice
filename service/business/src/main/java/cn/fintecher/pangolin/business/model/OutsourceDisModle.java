package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.CaseInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huyanmin on 2017/11/01.
 */
@Data
public class OutsourceDisModle {
    private String outsId;
    private BigDecimal outsAmt;
    private List<CaseInfo> caseInfos;
}
