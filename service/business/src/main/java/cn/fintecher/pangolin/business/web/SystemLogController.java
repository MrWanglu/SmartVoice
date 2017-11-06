package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.SystemLogRepository;
import cn.fintecher.pangolin.entity.QSystemLog;
import cn.fintecher.pangolin.entity.SystemLog;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.text.ParseException;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-10-9:28
 */
@RestController
@RequestMapping("/api/systemLogController")
@Api(value = "操作日志管理", description = "操作日志管理")
public class SystemLogController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(SystemLogController.class);
    private static final String ENTITY_NAME = "SystemLog";
    @Autowired
    private SystemLogRepository systemLogRepository;

    /**
     * @Description : 查询特定公司的日志
     */
    @GetMapping("/query")
    @ApiOperation(value = "查询特定公司的日志", notes = "查询特定公司的日志")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<SystemLog>> query(@RequestParam(required = false) String companyCode,
                                                 @RequestParam(required = false) String operator,
                                                 @RequestParam(required = false) String operatorTimeStart,
                                                 @RequestParam(required = false) String operatorTimeEnd,
                                                 @RequestParam(required = false) String remark,
                                                 @RequestHeader(value = "X-UserToken") String token,
                                                 @ApiIgnore Pageable pageable) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QSystemLog qSystemLog = QSystemLog.systemLog;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.isNull(companyCode)) {
            builder.and(qSystemLog.companyCode.isNull());
        } else {
            builder.and(qSystemLog.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(operator)) {
            builder.and(qSystemLog.operator.like(operator.concat("%")));
        }
        try {
            if (Objects.nonNull(operatorTimeStart)) {
                operatorTimeStart += " 00:00:00";
                builder.and(qSystemLog.operateTime.gt(ZWDateUtil.getUtilDate(operatorTimeStart, null)));
            }
        } catch (ParseException p) {
            p.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Acquisition time exception", "获取时间异常")).body(null);
        }
        try {
            if (Objects.nonNull(operatorTimeEnd)) {
                operatorTimeEnd += " 23:59:59";
                builder.and(qSystemLog.operateTime.lt(ZWDateUtil.getUtilDate(operatorTimeEnd, null)));
            }
        } catch (ParseException p) {
            p.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Acquisition time exception", "获取时间异常")).body(null);
        }
        if (Objects.nonNull(remark)) {
            builder.and(qSystemLog.remark.like(remark.concat("%")));
        }
        Page<SystemLog> page = systemLogRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(page);
    }
}
