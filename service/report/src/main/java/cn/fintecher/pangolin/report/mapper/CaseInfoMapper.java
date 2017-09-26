package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.model.CaseInfoParams;
import cn.fintecher.pangolin.report.model.CollectingCaseInfo;
import cn.fintecher.pangolin.report.model.CollectingCaseParams;
import cn.fintecher.pangolin.report.util.MyMapper;

import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:50 2017/8/1
 */
public interface CaseInfoMapper extends MyMapper<CaseInfo> {
//    List<CaseInfo> queryWaitCollectCase(@Param("deptCode") String deptCode,
//                                        @Param("companyCode") String companyCode);
    List<CaseInfo> queryWaitCollectCase(CaseInfoParams caseInfoParams);

    List<CaseInfo> queryWaitOwnCollectCase(CaseInfoParams caseInfoParams);

    List<CollectingCaseInfo> queryCollectingCase(CollectingCaseParams collectingCaseParams);

    void updateLngLat(Personal personal);

}
