package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.OutBackSourceRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import io.swagger.annotations.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by huyanmin
 * Description:
 * Date: 2017-08-31
 */

@RestController
@RequestMapping("/api/outbackSourceController")
@Api(value = "委外回款", description = "委外回款")
public class OutBackSourceController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(OutsourcePoolController.class);

    private static final String ENTITY_NAME = "OutBackSource";

    @Autowired
    private OutBackSourceRepository outbackSourceRepository;
    @Autowired
    private OutsourcePoolRepository outsourcePoolRepository;

    private static final String ENTITY_OUTBACK_FOLLOWUP_RECORD = "OutBackFollowupRecord";


    /**
     * @Description : 增加委外回款
     */
    @PostMapping("/createOutBackAmt")
    @ApiOperation(value = "委外案件回款", notes = "委外案件回款")
    public ResponseEntity<OutBackSource> createOutBackAmt(@RequestBody OutBackSource outBackSource,
                                                          @RequestHeader(value = "X-UserToken") String token) {

        outBackSource = (OutBackSource) EntityUtil.emptyValueToNull(outBackSource);
        log.debug("REST request to save department : {}", outBackSource);
        if (Objects.isNull(outBackSource.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The company logo cannot be empty", "公司标识不能为空")).body(null);
        }
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }

            String outId = outBackSource.getOutId();
            OutsourcePool outsourcePool = null;

            if (Objects.nonNull(outId)){
                outsourcePool = outsourcePoolRepository.findOne(outId);
                if(Objects.nonNull(outsourcePool)){

                    CaseInfo caseInfo = outsourcePool.getCaseInfo();
                    if (Objects.nonNull(caseInfo)){
                        outBackSource.setOutcaseId(outsourcePool.getCaseInfo().getId());
                    }
                    Integer operationType = outBackSource.getOperationType();
                    BigDecimal amt = outBackSource.getBackAmt();
                    //累加回款金额和操作状态
                    if (OutBackSource.operationType.OUTBACKAMT.getCode().equals(operationType) && amt!=null){
                        outsourcePool.setOutBackAmt(amt);//累加回款金额
                    }
                    outsourcePool.setOutoperationStatus(outBackSource.getOperationType());
                    outsourcePoolRepository.saveAndFlush(outsourcePool);//保存委外案件
                }
            }
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if(Objects.isNull(outBackSource.getCompanyCode())){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
            }else{
                outBackSource.setCompanyCode(user.getCompanyCode());
            }
            outBackSource.setOperator(user.getRealName());
            outBackSource.setOperateTime(ZWDateUtil.getNowDateTime());
            outbackSourceRepository.save(outBackSource);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", " 操作成功")).body(outBackSource);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("操作失败", "", e.getMessage())).body(null);
        }

    }

    /**
     * @Description 委外页面多条件查询回款跟进记录
     */
    @GetMapping("/getOutbackFollowupRecord/{outId}")
    @ApiOperation(value = "委外页面多条件查询回款跟进记录", notes = "委外页面多条件查询回款跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutBackSource>> getOutbackFollowupRecord(@PathVariable @ApiParam(value = "委外案件编号", required = true) String outId,
                                                                        @RequestParam String companyCode,
                                                                        @QuerydslPredicate(root = OutBackSource.class) Predicate predicate,
                                                                        @ApiIgnore Pageable pageable,
                                                                        @RequestHeader(value = "X-UserToken") String token) {

        log.debug("REST request to get outback source followup records by {outId}", outId);
        BooleanBuilder builder = new BooleanBuilder(predicate);
        try {
            User user = getUserByToken(token);
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
                builder.and(QOutBackSource.outBackSource.companyCode.eq(companyCode));
            }else{
                builder.and(QOutBackSource.outBackSource.companyCode.eq(user.getCompanyCode()));
            }

            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }

            if(Objects.nonNull(outId)){
                builder.and(QOutBackSource.outBackSource.outId.eq(outId));
            }
            Page<OutBackSource> page = outbackSourceRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/OutBackSourceController/getOutbackFollowupRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_OUTBACK_FOLLOWUP_RECORD, "OutbackFollowupRecord", "查询失败")).body(null);
        }
    }

}
