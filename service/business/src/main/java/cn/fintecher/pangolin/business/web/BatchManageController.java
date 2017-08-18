package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.SysNotice;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 批量管理
 * @Date : 2017/8/18.
 */
@RestController
@RequestMapping("/api/batchManageController")
@Api(value = "批量管理", description = "批量管理")
public class BatchManageController extends BaseController{
    private final Logger logger = LoggerFactory.getLogger(BatchManageController.class);
    private static final String ENTITY_NAME = "BatchManageController";

    @Inject
    private SysParamRepository sysParamRepository;

    @GetMapping("/getBatchSysNotice")
    @ApiOperation(notes = "首页批量系统公告", value = "首页批量系统公告")
    public ResponseEntity<SysNotice> getBatchSysNotice(@RequestHeader(value = "X-UserToken") String token) {
        logger.debug("Rest request to getBatchSysNotice");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ENTITY_NAME", "", e.getMessage())).body(null);
        }

        SysParam one = sysParamRepository.findOne(QSysParam.sysParam.companyCode.eq(user.getCompanyCode())
                .and(QSysParam.sysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STEP)));
        if (Objects.equals(one.getValue(),5)) { //步数为5-批量成功
            SysNotice sysNotice = new SysNotice();
            sysNotice.setTitle("批量完成");
            sysNotice.setContent("您于["+ ZWDateUtil.fomratterDate(one.getOperateTime(),null)+"]批量完成");
            return ResponseEntity.ok().body(sysNotice);
        } else {
            SysNotice sysNotice = new SysNotice();
            sysNotice.setTitle("批量失败");
            sysNotice.setContent("您于["+ ZWDateUtil.fomratterDate(one.getOperateTime(),null)+"]批量失败");
            return ResponseEntity.ok().body(sysNotice);
        }
    }
}
