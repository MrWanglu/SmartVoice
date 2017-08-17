package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.SmaRecordReport;
import cn.fintecher.pangolin.business.model.SmaRecordReturn;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(smaRecordReturns);
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
