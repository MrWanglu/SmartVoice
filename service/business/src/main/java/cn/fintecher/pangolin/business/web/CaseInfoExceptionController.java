package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.service.CaseInfoExceptionService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoDistributed;
import cn.fintecher.pangolin.entity.CaseInfoException;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @author : DuChao
 * @Description : 异常案件操作
 * @Date : 2017/7/20.
 */

@RestController
@RequestMapping("/api/CaseInfoExceptionController")
@Api(value = "CaseInfoExceptionController",description = "异常案件操作")
public class CaseInfoExceptionController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseInfoController.class);
    private static final String ENTITY_NAME = "caseInfoException";
    private static final String CASE_INFO_ENTITY = "caseInfo";
    @Autowired
    CaseInfoExceptionService caseInfoExceptionService;
    @Autowired
    RestTemplate restTemplate;

    /**
     * 获取所有异常案件
     * @return CaseInfoExceptionList
     */
    @GetMapping("/findAllCaseInfoException")
    @ApiOperation(value = "获取所有异常案件", notes = "获取所有异常案件")
    public List<CaseInfoException> getAllCaseInfoException(){
        log.debug("REST request to get all CaseInfoExceptions");
        return caseInfoExceptionService.getAllCaseInfoException();
    }

    /**
     * 新增案件
     * @param caseInfoExceptionId
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/addCaseInfoException")
    @ApiOperation(value = "新增案件", notes = "新增案件")
    public ResponseEntity<CaseInfoDistributed> addCaseInfo(@RequestBody String caseInfoExceptionId,
                                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws URISyntaxException{
        log.debug("REST request to update CaseInfo");
        CaseInfoDistributed caseInfoDistributed = caseInfoExceptionService.addCaseInfo(caseInfoExceptionId,getUserEntity(token));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(CASE_INFO_ENTITY,caseInfoDistributed.getId())).body(caseInfoDistributed);
    }
    /**
     * 更新案件
     * @param caseInfoExceptionId
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @PutMapping("/updateCaseInfoException")
    @ApiOperation(value = "更新案件", notes = "更新案件")
    public ResponseEntity<CaseInfo> updateCaseInfo(@RequestBody String caseInfoExceptionId,
                                                   @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws URISyntaxException {
        log.debug("REST request to update CaseInfoException");
        CaseInfo caseInfo= caseInfoExceptionService.updateCaseInfoException(caseInfoExceptionId,getUserEntity(token));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(CASE_INFO_ENTITY,caseInfo.getId().toString())).body(caseInfo);
    }

    private User getUserEntity(String token){
        ResponseEntity<User> userResponseEntity=null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return userResponseEntity.getBody();
    }
    /**
     * 删除异常池案件
     * @param caseInfoExceptionId
     * @return
     */
    @DeleteMapping("/deleteCaseInfoException")
    @ApiOperation(value = "删除异常池案件", notes = "删除异常池案件")
    public ResponseEntity<Void> deleteCaseInfoException(@RequestBody String caseInfoExceptionId) {
        log.debug("REST request to delete caseInfoException : {}",caseInfoExceptionId);
        caseInfoExceptionService.deleteCaseInfoException(caseInfoExceptionId);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, caseInfoExceptionId)).build();
    }

}
