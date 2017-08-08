package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.inject.Inject;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/**
 * @author : gaobeibei
 * @Description : 案件跟进记录APP
 * @Date : 11:28 2017/7/27
 */

@RestController
@RequestMapping(value = "/api/CaseFollowupAppController")
@Api(value = "APP案件跟进记录", description = "APP案件跟进记录")
public class CaseFollowupAppController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseFollowupAppController.class);

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @GetMapping("/getAllFollowupsForApp")
    @ApiOperation(value = "APP查询案件跟进记录", notes = "APP查询案件跟进记录")
    public ResponseEntity<Page<CaseFollowupRecord>> getAllFollowupsForApp(
            @ApiParam(value = "跟进最小日期") @RequestParam(required = false) String follUpMinTime,
            @ApiParam(value = "跟进最大日期") @RequestParam(required = false) String follUpMaxTime,
            @ApiParam(value = "跟进形式") @RequestParam(required = false) Integer follType,
            @ApiParam(value = "催收反馈") @RequestParam(required = false) Integer follFeedback,
            @ApiParam(value = "跟进来源") @RequestParam(required = false) Integer follSource,
            @ApiParam(value = "案件编号", required = true) @RequestParam String caseId, Pageable pageable) throws Exception{
        try {
            if (Objects.nonNull(follUpMinTime)) {
                follUpMinTime = follUpMinTime + " 00:00:00";
            }
            if (Objects.nonNull(follUpMaxTime)) {
                follUpMaxTime = follUpMaxTime + " 23:59:59";
            }
            Date minTime = ZWDateUtil.getUtilDate(follUpMinTime, "yyyy-MM-dd HH:mm:ss");
            Date maxTime = ZWDateUtil.getUtilDate(follUpMaxTime, "yyyy-MM-dd HH:mm:ss");
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseId.eq(caseId));
            if (Objects.nonNull(maxTime)) {
                builder.and(QCaseFollowupRecord.caseFollowupRecord.operatorTime.before(maxTime));
            }
            if(Objects.nonNull(minTime)){
                builder.and(QCaseFollowupRecord.caseFollowupRecord.operatorTime.after(minTime));
            }
            if(Objects.nonNull(follType)){
                builder.and((QCaseFollowupRecord.caseFollowupRecord.type.eq(follType)));
            }
            if(Objects.nonNull(follSource)){
                builder.and(QCaseFollowupRecord.caseFollowupRecord.source.eq(follSource));
            }
            if(Objects.nonNull(follFeedback)){
                builder.and(QCaseFollowupRecord.caseFollowupRecord.collectionFeedback.eq(follFeedback));
            }
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/CaseFollowupAppController");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "caseFollowupRecord", e.getMessage())).body(null);
        }
    }
}
