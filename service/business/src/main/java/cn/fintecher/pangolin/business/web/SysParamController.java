package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-04-9:33
 */
@RestController
@RequestMapping("/api/sysParamController")
@Api(value = "系统参数", description = "系统参数")
public class SysParamController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(SysParamController.class);
    private static final String ENTITY_NAME = "SysParam";

    @Autowired
    private SysParamRepository sysParamRepository;

    /**
     * @Description : 系统参数带条件的分页查询
     */
    @GetMapping("/query")
    @ApiOperation(value = "系统参数带条件的分页查询", notes = "系统参数带条件的分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<SysParam>> getDepartAllUser(@RequestParam(required = false) String name,
                                                           @RequestParam(required = false) String code,
                                                           @RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false) String type,
                                                           @RequestParam(required = false) String value,
                                                           @RequestParam(required = false) Integer sign,
                                                           @RequestParam(required = false) String companyCode,
                                                           @ApiIgnore Pageable pageable,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QSysParam qSysParam = QSysParam.sysParam;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(name)) {
            builder.and(qSysParam.name.like(name.concat("%")));
        }
        if (Objects.nonNull(code)) {
            builder.and(qSysParam.code.like(code.concat("%")));
        }
        if (Objects.nonNull(status)) {
            builder.and(qSysParam.status.eq(status));
        }
        if (Objects.nonNull(type)) {
            builder.and(qSysParam.type.like(type.concat("%")));
        }
        if (Objects.nonNull(value)) {
            builder.and(qSysParam.value.eq(value));
        }
        if (Objects.nonNull(sign)) {
            builder.and(qSysParam.sign.eq(sign));
        }
        if (Objects.nonNull(companyCode)) {
            builder.and(qSysParam.companyCode.eq(companyCode));
        }
        Page<SysParam> page = sysParamRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新增系统参数
     */
    @PostMapping("/createSysParam")
    @ApiOperation(value = "新增/修改系统参数", notes = "新增系统参数")
    public ResponseEntity<SysParam> createSysParam(@Validated @RequestBody SysParam sysParam,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(sysParam.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "companyCode code does not exist", "公司码为空")).body(null);
        }
        SysParam sysParam1 = sysParamRepository.save(sysParam);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(sysParam1);
    }
}
