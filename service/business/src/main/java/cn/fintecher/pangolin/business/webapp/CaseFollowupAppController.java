package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.CaseFollowupParams;
import cn.fintecher.pangolin.business.repository.CaseFlowupFileRepository;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.util.*;

/**
 * @author : gaobeibei
 * @Description : 案件跟进记录APP
 * @Date : 11:28 2017/7/27
 */

@RestController
@RequestMapping(value = "/api/caseFollowupAppController")
@Api(value = "APP案件跟进记录", description = "APP案件跟进记录")
public class CaseFollowupAppController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseFollowupAppController.class);

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    CaseFlowupFileRepository caseFlowupFileRepository;

    @Inject
    CaseInfoService caseInfoService;

    @Inject
    CaseInfoRepository caseInfoRepository;

    @GetMapping("/getAllFollowupsForApp")
    @ApiOperation(value = "APP查询案件跟进记录", notes = "APP查询案件跟进记录")
    public ResponseEntity<Page<CaseFollowupRecord>> getAllFollowupsForApp(@ApiParam(value = "案件ID", required = true) @RequestParam String id, Pageable pageable) throws Exception{
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseFollowupRecord.caseFollowupRecord.caseId.eq(id));
        Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
        for(CaseFollowupRecord caseFollowupRecord : page){
            Iterable<CaseFlowupFile> iterable = caseFlowupFileRepository.findAll(new BooleanBuilder().and(QCaseFlowupFile.caseFlowupFile.followupId.id.eq(caseFollowupRecord.getId())));
            List<String> fileIds = new ArrayList<>();
            iterable.forEach(e->{
                fileIds.add(e.getFileid());
            });
            caseFollowupRecord.setFileIds(fileIds);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/CaseFollowupAppController");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/getPayPromiseInfo")
    @ApiOperation(value = "根据案件编号查询承诺信息(APP)", notes = "根据案件编号查询承诺信息(APP)")
    public ResponseEntity<CaseFollowupRecord> getPayPromiseInfo(@ApiParam(value = "案件编号", required = true) @RequestParam String id) {
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseId.eq(id));
            builder.and(QCaseFollowupRecord.caseFollowupRecord.promiseFlag.eq(1));
            Iterator<CaseFollowupRecord> iterator = caseFollowupRecordRepository.findAll(builder, new Sort(Sort.Direction.DESC, "operatorTime")).iterator();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "CaseFollowupRecord")).body(iterator.next());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseFollowupRecord", "CaseFollowupRecord", "")).body(null);
        }
    }

    /**
     * @Description APP添加跟进记录
     */
    @PostMapping("/saveFollowupRecordForApp")
    @ApiOperation(value = "APP添加跟进记录", notes = "APP添加跟进记录")
    public ResponseEntity<CaseFollowupRecord> saveFollowupRecord(@RequestBody CaseFollowupParams caseFollowupParams,
                                                                 @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save {}", caseFollowupParams);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(caseFollowupParams.getCaseId());
        if(Objects.equals(caseInfo.getAssistFlag(),CaseInfo.AssistFlag.NO_ASSIST.getValue())){
            caseFollowupParams.setSource(CaseFollowupRecord.Source.VISIT.getValue());
            caseFollowupParams.setType(CaseFollowupRecord.Type.VISIT.getValue());
        }else{
            caseFollowupParams.setSource(CaseFollowupRecord.Source.ASSIST.getValue());
            caseFollowupParams.setType(CaseFollowupRecord.Type.ASSIST.getValue());
        }
        CaseFollowupRecord result = caseInfoService.saveFollowupRecord(caseFollowupParams, user);
        if(Objects.nonNull(caseFollowupParams.getFileIds())){
            List<String> fileIds = caseFollowupParams.getFileIds();
            Iterator<String> iterator = fileIds.iterator();
            while(iterator.hasNext()){
                CaseFlowupFile caseFlowupFile = new CaseFlowupFile();
                caseFlowupFile.setFollowupId(result);
                caseFlowupFile.setFileid(iterator.next());
                caseFlowupFile.setOperator(user.getId());
                caseFlowupFile.setOperatorName(user.getUserName());
                caseFlowupFile.setOperatorTime(ZWDateUtil.getNowDateTime());
                caseFlowupFileRepository.save(caseFlowupFile);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("添加跟进记录成功", "CaseFollowupRecord")).body(result);
    }
}
