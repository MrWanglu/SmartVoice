package cn.fintecher.pangolin.report.web;


import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.model.CaseInfoParams;
import cn.fintecher.pangolin.report.model.CollectingCaseInfo;
import cn.fintecher.pangolin.report.model.CollectingCaseParams;
import cn.fintecher.pangolin.report.model.MapModel;
import cn.fintecher.pangolin.report.service.AccMapService;
import cn.fintecher.pangolin.report.service.CaseInfoService;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:59 2017/8/1
 */
@RestController
@RequestMapping("/api/caseInfoReportController")
@Api(description = "案件信息操作")
public class CaseInfoReportController extends BaseController{

    Logger logger=LoggerFactory.getLogger(CaseInfoReportController.class);
    @Autowired
    CaseInfoService caseInfoService;
    @Autowired
    AccMapService accMapService;
    @GetMapping("/getCaseInfoAll")
    public ResponseEntity<List<CaseInfo>> getCaseInfoAll() throws URISyntaxException {
        List<CaseInfo> caseInfoList=caseInfoService.getAll(null);

      return  ResponseEntity.created(new URI("/getCaseAll"))
                .headers(HeaderUtil.createEntityCreationAlert("测试", String.valueOf(caseInfoList.size())))
                .body(caseInfoList);
    }

    @GetMapping("/queryCaseDetail")
    @ApiOperation(value = "待催收案件查询", notes = "待催收案件查询")
    public ResponseEntity<PageInfo>  getCaseDetail(@RequestHeader(value = "X-UserToken") String token,
                                                        @RequestParam(required = true)@ApiParam(value = "页数") Integer page,
                                                        @RequestParam(required = true)@ApiParam(value = "大小") Integer size,
                                                        @RequestParam(required = false) @ApiParam(value = "客户名称") String name,
                                                        @RequestParam(required = false) @ApiParam(value = "地址") String address) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        if(Objects.equals(user.getType(),User.Type.TEL.getValue())){
            return ResponseEntity.ok().body(null);
        }
        if(!Objects.equals(user.getType(),User.Type.VISIT.getValue())
                && !Objects.equals(user.getType(),User.Type.SYNTHESIZE.getValue())){
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功","")).body(null);
        }
        List<CaseInfo> list = new ArrayList<>();
        CaseInfoParams caseInfoParams = new CaseInfoParams();
        caseInfoParams.setCompanyCode(user.getCompanyCode());
        caseInfoParams.setCollector(user.getId());
        caseInfoParams.setDeptCode(user.getDepartment().getCode());
        if(Objects.nonNull(name)){
            caseInfoParams.setName(name);
        }
        if(Objects.nonNull(address)){
            caseInfoParams.setAddress(address);
        }
        list = caseInfoService.queryWaitCollectCase(caseInfoParams,page,size,user);
        PageInfo pageInfo = new PageInfo(list);
        List<CaseInfo> lists = new ArrayList<>();

        for(int i=0; i<list.size(); i++){
            CaseInfo caseInfo = list.get(i);
            Personal personal = caseInfo.getPersonalInfo();
            if(Objects.isNull(personal)){
                continue;
            }
            if(Objects.isNull(personal.getLongitude())
                    || Objects.isNull(personal.getLatitude())){
                try{
                    MapModel model = accMapService.getAddLngLat(personal.getLocalHomeAddress());
                    personal.setLatitude(BigDecimal.valueOf(model.getLatitude()));
                    personal.setLongitude(BigDecimal.valueOf(model.getLongitude()));
                    caseInfoService.updateLngLat(personal);
                    caseInfo.setPersonalInfo(personal);
                }catch(Exception e1){
                    e1.getMessage();
                }
            }
         lists.add(caseInfo);
        }
        PageInfo pageInfos = new PageInfo(lists);
        pageInfos.setPages(pageInfo.getPages());
        pageInfos.setTotal(pageInfo.getTotal());
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功","")).body(pageInfos);
    }
    @GetMapping("/queryCollectingCase")
    @ApiOperation(value = "PC催收中案件查询", notes = "PC催收中案件查询")
    public ResponseEntity<PageInfo>  queryCollectingCase(@RequestHeader(value = "X-UserToken") String token,
                                                   @RequestParam(required = true)@ApiParam(value = "页数") Integer page,
                                                   @RequestParam(required = true)@ApiParam(value = "大小") Integer size,
                                                   @RequestParam(required = false) @ApiParam(value = "批次号") String batchNumber,
                                                   @RequestParam(required = false) @ApiParam(value = "委托方") String principalId,
                                                   @RequestParam(required = false) @ApiParam(value = "委案日期") String delegationDate,
                                                   @RequestParam(required = false) @ApiParam(value = "委案日期") String closeDate,
                                                   @RequestParam(required = false) @ApiParam(value = "公司CODE") String companyCode) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }

        CollectingCaseParams collectingCaseParams = new CollectingCaseParams();
        collectingCaseParams.setDeptCode(user.getDepartment().getCode());
        if(Objects.nonNull(companyCode)){
            collectingCaseParams.setCompanyCode(companyCode);
        }else{
            collectingCaseParams.setCompanyCode(user.getCompanyCode());
        }
        if(Objects.nonNull(batchNumber)){
            collectingCaseParams.setBatchNumber(batchNumber);
        }
        if(Objects.nonNull(principalId)){
            collectingCaseParams.setPrincipalId(principalId);
        }
        if(Objects.nonNull(delegationDate)){
            collectingCaseParams.setDelegationDate(delegationDate);
        }
        if(Objects.nonNull(closeDate)){
            collectingCaseParams.setCloseDate(closeDate);
        }
        List<CollectingCaseInfo> list = caseInfoService.queryCollectingCase(collectingCaseParams,page,size);
        PageInfo pageInfo = new PageInfo(list);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功","")).body(pageInfo);
    }

}
