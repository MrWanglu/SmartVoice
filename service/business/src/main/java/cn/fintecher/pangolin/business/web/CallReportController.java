package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.SmaRecordReport;
import cn.fintecher.pangolin.business.model.SmaRecordReturn;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-17-12:12
 */
@RestController
@RequestMapping("/api/callReportController")
@Api(value = "对呼报表", description = "对呼报表")
public class CallReportController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String ENTITY_NAME = "CallReport";
    @Autowired
    private CaseFollowupRecordRepository caseFollowupRecordRepository;

    /**
     * @Description : 查询对呼数据
     */

    @GetMapping("/query")
    @ApiOperation(value = "查询对呼数据", notes = "查询对呼数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseFollowupRecord>> query(@RequestParam String companyCode,
                                                          @RequestParam Integer callType,
                                                          @RequestParam(required = false) String userName,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qCaseFollowupRecord.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(userName)) {
            builder.and(qCaseFollowupRecord.operatorName.like(userName.concat("%")));
        }
        if (Objects.nonNull(callType)) {
            builder.and(qCaseFollowupRecord.callType.eq(callType));
        }
        Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "operation successfully")).body(page);
    }

    /**
     * @Description : 中通天鸿 164 双向外呼通话个数统计
     */
    @PostMapping("/getCountSmaRecord")
    @ApiOperation(notes = "双向外呼通话个数统计", value = "双向外呼通话个数统计")
    public ResponseEntity<List<SmaRecordReturn>> getCountSmaRecord(@RequestBody SmaRecordReport request) {
        try {
            List<Object[]> objects = caseFollowupRecordRepository.getCountSmaRecord(request.getStartTime(), request.getEndTime(), request.getCompanyCode());
            List<SmaRecordReturn> smaRecordReturns = new ArrayList<>();
            for (Object[] objects1 : objects) {
                SmaRecordReturn smaRecordReturn = new SmaRecordReturn();
                smaRecordReturn.setParameter(objects1[0].toString());
                smaRecordReturn.setUserName(objects1[1].toString());
                smaRecordReturn.setRealName(objects1[2].toString());
                smaRecordReturns.add(smaRecordReturn);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "operation successfully")).body(smaRecordReturns);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operation failure", "操作失败")).body(null);
        }
    }

    /**
     * @Description : 中通天鸿 164 双向外呼通话时长统计
     */
    @PostMapping("/getCountTimeSmaRecord")
    @ApiOperation(notes = "双向外呼通话时长统计", value = "双向外呼通话时长统计")
    public ResponseEntity<List<SmaRecordReturn>> getCountTimeSmaRecord(@RequestBody SmaRecordReport request) {
        try {
            List<Object[]> objects = caseFollowupRecordRepository.getCountTimeSmaRecord(request.getStartTime(), request.getEndTime(), request.getCompanyCode());
            List<SmaRecordReturn> smaRecordReturns = new ArrayList<>();
            for (Object[] objects1 : objects) {
                SmaRecordReturn smaRecordReturn = new SmaRecordReturn();
                smaRecordReturn.setParameter(objects1[0].toString());
                smaRecordReturn.setUserName(objects1[1].toString());
                smaRecordReturn.setRealName(objects1[2].toString());
                smaRecordReturns.add(smaRecordReturn);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(smaRecordReturns);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operation failure", "操作失败")).body(null);
        }
    }
}
