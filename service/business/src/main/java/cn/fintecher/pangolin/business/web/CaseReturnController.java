package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.RecoverCaseParams;
import cn.fintecher.pangolin.business.service.RecoverCaseService;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * Created by sunyanping on 2017/9/27.
 */
@RestController
@RequestMapping("/api/caseReturnController")
@Api(value = "CaseReturnController", description = "回收案件操作")
public class CaseReturnController extends BaseController {
    private static final String ENTITY_NAME = "CaseReturnController";
    private final Logger logger = LoggerFactory.getLogger(CaseReturnController.class);

    @Inject
    private RecoverCaseService recoverCaseService;

    @PostMapping("/recoverCase")
    @ApiOperation(notes = "回收案件", value = "回收案件")
    public ResponseEntity recoverCase(@RequestHeader(value = "X-UserToken") String token,
                                      @RequestBody RecoverCaseParams recoverCaseParams) {
        try {
            User user = getUserByToken(token);
            recoverCaseService.recoverCase(recoverCaseParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }

    }

}
