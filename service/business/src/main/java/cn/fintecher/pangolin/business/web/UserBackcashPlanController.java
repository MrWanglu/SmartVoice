package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.UserBackcashPlanRepository;
import cn.fintecher.pangolin.entity.*;
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

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-02-10:26
 */
@RestController
@RequestMapping("/api/userBackcashPlanController")
@Api(value = "用户计划回款金额", description = "用户计划回款金额")
public class UserBackcashPlanController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(UserBackcashPlanController.class);
    private static final String ENTITY_NAME = "UserBackcashPlan";
    @Autowired
    private UserBackcashPlanRepository userBackcashPlanRepository;

    /**
     * @Description : 查询用户计划回款金额
     */

    @PostMapping("/query")
    @ApiOperation(value = "查询用户计划回款金额", notes = "查询用户计划回款金额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<UserBackcashPlan>> query(@RequestParam(required = false) String userName,
                                                        @RequestParam(required = false) String realName,
                                                        @RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QUserBackcashPlan qUserBackcashPlan = QUserBackcashPlan.userBackcashPlan;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(userName)) {
            builder.and(qUserBackcashPlan.userName.eq(userName));
        }
        if (Objects.nonNull(realName)) {
            builder.and(qUserBackcashPlan.realName.like(realName.concat("%")));
        }
        if (Objects.nonNull(year)) {
            builder.and(qUserBackcashPlan.year.eq(year));
        }
        if (Objects.nonNull(month)) {
            builder.and(qUserBackcashPlan.month.eq(month));
        }
        Page<UserBackcashPlan> page = userBackcashPlanRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }
}
