package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.SysNotice;
import cn.fintecher.pangolin.business.repository.BatchManageRepository;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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

    @Autowired
    private BatchManageRepository batchManageRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("/getBatchSysNotice")
    @ApiOperation(notes = "首页批量系统公告", value = "首页批量系统公告")
    public ResponseEntity<SysNotice> getBatchSysNotice(@RequestHeader(value = "X-UserToken") String token) {
        logger.debug("Rest request to getBatchSysNotice");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        SysNotice sysNotice = new SysNotice();
        if (Objects.isNull(user.getCompanyCode())) {
            Iterable<SysParam> all = sysParamRepository.findAll(QSysParam.sysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STEP));
            StringBuilder sb = new StringBuilder();
            for (SysParam sysParam : all) {
                if (!Objects.equals(sysParam.getValue(), "5")) {
                    sb.append(companyRepository.findOne(QCompany.company.code.eq(sysParam.getCompanyCode())).getChinaName().concat("、"));
                }
            }
            String sbn = sb.substring(0,sb.length()-1);
            if (StringUtils.length(sbn) == 0) {
                sysNotice.setTitle("批量完成");
                sysNotice.setContent("各公司的批量完成");
                return ResponseEntity.ok().body(sysNotice);
            }
            sysNotice.setTitle("批量失败");
            sysNotice.setContent("公司" + sbn + "的批量失败");
            return ResponseEntity.ok().body(sysNotice);
        }

        SysParam one = sysParamRepository.findOne(QSysParam.sysParam.companyCode.eq(user.getCompanyCode())
                .and(QSysParam.sysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STEP)));
        if (Objects.equals(one.getValue(),"5")) { //步数为5-批量成功
            sysNotice.setTitle("批量完成");
            sysNotice.setContent("您于["+ ZWDateUtil.fomratterDate(one.getOperateTime(),null)+"]批量完成");
            return ResponseEntity.ok().body(sysNotice);
        } else {
            sysNotice.setTitle("批量失败");
            sysNotice.setContent("您于["+ ZWDateUtil.fomratterDate(one.getOperateTime(),null)+"]批量失败");
            return ResponseEntity.ok().body(sysNotice);
        }
    }

    @GetMapping("/getBatchManage")
    @ApiOperation(value = "多条件查询批量处理记录", notes = "多条件查询批量处理记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<BatchManage>> getBatchManage(@QuerydslPredicate(root = BatchManage.class) Predicate predicate,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"user not login","用户未登录")).body(null);
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        Page<BatchManage> page = batchManageRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功","batchManageController")).body(page);
    }

}
