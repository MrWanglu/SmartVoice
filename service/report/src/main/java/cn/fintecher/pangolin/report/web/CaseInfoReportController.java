package cn.fintecher.pangolin.report.web;


import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.model.MapModel;
import cn.fintecher.pangolin.report.service.AccMapService;
import cn.fintecher.pangolin.report.service.CaseInfoService;
import cn.fintecher.pangolin.web.HeaderUtil;
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
@Api(description = "委托方数据操作")
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
    public ResponseEntity<List<CaseInfo>> getCaseDetail(@RequestHeader(value = "X-UserToken") String token,
                                                        @RequestParam(required = true)@ApiParam(value = "页数") Integer page,
                                                        @RequestParam(required = true)@ApiParam(value = "大小") Integer size) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        if(Objects.equals(user.getType(),User.Type.TEL.getValue())){
            return ResponseEntity.ok().body(null);
        }
        List<CaseInfo> list = new ArrayList<>();
        list = caseInfoService.queryWaitCollectCase(user,page,size);
        for(int i=0; i<list.size(); i++){
            CaseInfo caseInfo = list.get(i);
            if(Objects.isNull(caseInfo.getPersonalInfo().getLongitude())
                    || Objects.isNull(caseInfo.getPersonalInfo().getLatitude())){
                try{
                    MapModel model = accMapService.getAddLngLat(caseInfo.getPersonalInfo().getLocalHomeAddress());
                    caseInfo.getPersonalInfo().setLatitude(BigDecimal.valueOf(model.getLatitude()));
                    caseInfo.getPersonalInfo().setLongitude(BigDecimal.valueOf(model.getLongitude()));
                    caseInfoService.updateLngLat(caseInfo.getPersonalInfo());
                }catch(Exception e1){
                    e1.getMessage();
                }
            }
        }
        return ResponseEntity.ok().body(list);
    }

}
