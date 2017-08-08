package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 案件分配
 * @Date 15:50 2017/8/7
 */
@RestController
@RequestMapping(value = "/api/caseInfoDistributeController")
@Api(value = "案件分配", description = "案件分配")
public class CaseInfoDistributeController extends BaseController {

    private static final String ENTITY_NAME = "aseInfoDistributeController";
    Logger logger=LoggerFactory.getLogger(CaseInfoDistributeController.class);

    @Autowired
    CaseInfoService caseInfoService;

    @RequestMapping(value = "/distributeCeaseInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "案件分配(机构时传入机构的ID)", notes = "案件分配")
    public ResponseEntity distributeCeaseInfo(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                              @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user=getUserByToken(token);
            caseInfoService.distributeCeaseInfo(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功",ENTITY_NAME)).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            String msg= Objects.isNull(e.getMessage()) ? "系统异常" : e.getMessage();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"error",msg)).body(null);
        }

    }

}
