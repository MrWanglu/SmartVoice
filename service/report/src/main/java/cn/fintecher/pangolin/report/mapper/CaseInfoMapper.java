package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.model.*;
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
            @Param("collectionType") String collectionType,
            @Param("sort") String sort,
            @Param("code") String code,
            @Param("collectionStatusList") String collectionStatusList,
            @Param("collectionStatus") Integer collectionStatus,
            @Param("parentAreaId") Integer parentAreaId,
            @Param("areaId") Integer areaId,
            @Param("type") Integer type,
            @Param("isManager") Integer isManager,
            @Param("userId") String userId,
            @Param("realPayMaxAmt")BigDecimal realPayMaxAmt,
            @Param("realPayMinAmt") BigDecimal realPayMinAmt);

    /**
     * @Description 多条件查询协催案件信息
     */
    List<CaseAssistModel> getCaseAssistByCondition(@Param("personalName") String personalName,
                                                   @Param("mobileNo") String mobileNo,
                                                   @Param("overdueMaxAmount") BigDecimal overdueMaxAmount,
                                                   @Param("overdueMinAmount") BigDecimal overdueMinAmount,
                                                   @Param("assistStatusList") String assistStatusList,
                                                   @Param("deptCode") String deptCode,
                                                   @Param("sort") String sort,
                                                   @Param("isManager") Integer isManager,
                                                   @Param("userId") String userId);

    /**
     * @Description 多条件查询协催案件信息
     */
    List<CaseInfoModel> getInnerCaseInfoByCondition(CaseInfoConditionParams caseInfoConditionParams);
}
