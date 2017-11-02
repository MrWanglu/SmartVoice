package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.model.CaseInfoModel;
import cn.fintecher.pangolin.report.model.CaseInfoParams;
import cn.fintecher.pangolin.report.model.CollectingCaseInfo;
import cn.fintecher.pangolin.report.model.CollectingCaseParams;
import cn.fintecher.pangolin.report.util.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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

    /**
     * @Description 多条件查询案件信息
     */
    List<CaseInfoModel> getCaseInfoByCondition(
            @Param("personalName") String personalName,
            @Param("mobileNo") String mobileNo,
            @Param("deptCode") String deptCode,
            @Param("collectorName") String collectorName,
            @Param("overdueMaxAmt") BigDecimal overdueMaxAmt,
            @Param("overdueMinAmt") BigDecimal overdueMinAmt,
            @Param("payStatus") String payStatus,
            @Param("overMaxDay") Integer overMaxDay,
            @Param("overMinDay") Integer overMinDay,
            @Param("batchNumber") String batchNumber,
            @Param("principalId") String principalId,
            @Param("idCard") String idCard,
            @Param("feedBack") Integer feedBack,
            @Param("assistWay") Integer assistWay,
            @Param("caseMark") Integer caseMark,
            @Param("collectionType") Integer collectionType,
            @Param("sort") String sort,
            @Param("code") String code,
            @Param("collectionStatusList") String collectionStatusList,
            @Param("collectionStatus") Integer collectionStatus);

}
