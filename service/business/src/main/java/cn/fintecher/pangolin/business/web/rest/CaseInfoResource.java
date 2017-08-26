package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseRepairRepository;
import cn.fintecher.pangolin.business.repository.CaseTurnRecordRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;


/**
 * Created by luqiang on 2017/8/8.
 */
@RestController
@RequestMapping("/api/caseInfoResource")
@Api(description = "领取客户池资源")
public class CaseInfoResource {
    private final Logger log = LoggerFactory.getLogger(CaseInfoResource.class);
    @Inject
    CaseInfoService caseInfoService;
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoDistributedRepository caseInfoDistributedRepository;
    @Inject
    CaseTurnRecordRepository caseTurnRecordRepository;
    @Inject
    private  CaseRepairRepository caseRepairRepository;

    @GetMapping("/getAllCaseInfo")
    @ApiOperation(value = "查询所有已确认案件", notes = "查询所有已确认案件")
    public ResponseEntity<Iterable<CaseInfoDistributed>> getAllCaseInfo(@RequestParam String companyCode) throws URISyntaxException {
        log.debug("REST request to get all of CaseInfo");
        Iterable<CaseInfoDistributed> caseInfoList = caseInfoDistributedRepository.findAll(QCaseInfoDistributed.caseInfoDistributed.companyCode.eq(companyCode));
        return new ResponseEntity<>(caseInfoList, HttpStatus.OK);
    }
    @PostMapping("/saveCaseInfo")
    @ApiOperation(value = "保存案件信息", notes = "保存案件信息")
    public ResponseEntity<CaseInfo> saveCaseInfo(@RequestBody List<CaseInfo> accReceivePool) throws URISyntaxException {
        caseInfoRepository.save(accReceivePool);
        return new ResponseEntity<>(HttpStatus.OK);//CaseRepair
    }
    @PostMapping("/saveCaseInfoRecord")
    @ApiOperation(value = "保存流转记录", notes = "保存流转记录")
    public ResponseEntity<CaseTurnRecord> saveCaseInfoRecord(@RequestBody List<CaseTurnRecord> caseTurnRecord) throws URISyntaxException {
        caseTurnRecordRepository.save(caseTurnRecord);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/saveRepair")
    @ApiOperation(value = "保存修复信息", notes = "保存修复信息")
    public ResponseEntity<CaseRepair> saveRepair(@RequestBody List<CaseRepair> caseRepair) throws URISyntaxException {
        caseRepairRepository.save(caseRepair);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @DeleteMapping("/deleteCaseInfoDistributed")
    @ApiOperation(value = "删除已分配的案件", notes = "删除已分配的案件")
    public ResponseEntity<Void> deleteCaseInfoDistributed(@ApiParam(value = "案件id", required = true) @RequestParam String id) throws URISyntaxException {
        log.debug("REST request to delete caseInfo by id", id);
        if (id == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoDistributed",
                    "id is null", "id is null")).body(null);
        }
        caseInfoDistributedRepository.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
