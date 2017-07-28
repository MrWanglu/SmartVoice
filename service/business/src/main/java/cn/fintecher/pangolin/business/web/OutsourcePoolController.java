package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.OutCaseIdList;
import cn.fintecher.pangolin.business.model.OutsourceInfo;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-10:14
 */
@RestController
@RequestMapping("/api/outsourcePoolController")
@Api(value = "委外管理", description = "委外管理")
public class OutsourcePoolController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(OutsourcePoolController.class);
    //案件批次号最大99999（5位）
    public final static String CASE_SEQ = "caseSeq";
    @Autowired
    private OutsourceRepository outsourceRepository;
    @Autowired
    private BatchSeqService batchSeqService;
    @Autowired
    private CaseInfoRepository caseInfoRepository;
    @Autowired
    private OutsourcePoolRepository outsourcePoolRepository;
    @Autowired
    private OutsourceRecordRepository outsourceRecordRepository;
    private static final String ENTITY_NAME = "OutSource";
    private static final String ENTITY_NAME1 = "OutSourcePool";

    @PostMapping("/outsource")
    @ApiOperation(value = "委外处理", notes = "委外处理")
    public ResponseEntity<Void> batchDistribution(@RequestBody OutsourceInfo outsourceInfo, @RequestHeader(value = "X-UserToken") String token) {
        try {
                List<String> caseIds = outsourceInfo.getCaseIds();//待委外的案件id集合
                List<OutsourceRecord> outsourceRecords = new ArrayList<>();//待保存的案件委外记录集合
                List<CaseInfo> caseInfos = new ArrayList<>();//待保存的委外案件集合
                List<OutsourcePool> outsourcePools = new ArrayList<>();//待保存的流转记录集合
                User user = getUserByToken(token);
                if (Objects.isNull(user)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "获取不到登录人信息")).body(null);
                }
                LabelValue seqResult = batchSeqService.nextSeq(CASE_SEQ, 5);
                String ouorBatch = seqResult.getValue();
                for (String cupoId : caseIds) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(cupoId);
                    if (CaseInfo.CollectionStatus.CASE_OVER.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已结案案件不能再委外")).body(null);
                    }
                    if (CaseInfo.CollectionStatus.REPAID.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已还款案件不能再委外")).body(null);
                    }
                    Outsource outsource = outsourceRepository.findOne(outsourceInfo.getOutsId());
                    OutsourceRecord outsourceRecord = new OutsourceRecord();
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OUT.getValue());
                    outsourceRecord.setCaseInfo(caseInfo);
                    outsourceRecord.setOutsource(outsource);
                    outsourceRecord.setCreateTime(ZWDateUtil.getNowDateTime());
                    outsourceRecord.setCreator(user.getUserName());
                    outsourceRecord.setFlag(0);//默认正常
                    outsourceRecord.setOuorBatch(ouorBatch);//批次号
                    outsourceRecords.add(outsourceRecord);
                    //将原案件改为已结案
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue());//已委外
                    caseInfo.setEndType(CaseInfo.EndType.OUTSIDE_CLOSED.getValue());//委外结案
                    caseInfo.setOperator(user);
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseInfo.setEndRemark("委外结案");//结案说明
                    caseInfos.add(caseInfo);

                    //保存委外案件
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setOutsource(outsource);
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setOperator(user.getUserName());
                    outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());
                    outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDING.getCode());//委外中
                    outsourcePool.setOutTime(ZWDateUtil.getNowDateTime());
                    outsourcePools.add(outsourcePool);
                }
                //批量保存
                caseInfoRepository.save(caseInfos);
                outsourcePoolRepository.save(outsourcePools);
                outsourceRecordRepository.save(outsourceRecords);
                return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外失败", ENTITY_NAME, e.getMessage())).body(null);
        }

    }

    /**
     * @Description : 查询委外案件
     */
    @PostMapping("/query")
    @ApiOperation(value = "查询委外案件", notes = "查询委外案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutsourcePool>> query(@RequestParam(required = false) @ApiParam(value = "最小逾期天数") Integer overDayMin,
                                                     @RequestParam(required = false) @ApiParam(value = "最大逾期天数") Integer overDayMax,
                                                     @RequestParam(required = false) @ApiParam(value = "委外方") String outsName,
                                                     @RequestParam(required = false) @ApiParam(value = "催收状态") Integer oupoStatus,
                                                     @RequestParam(required = false) @ApiParam(value = "最小案件金额") BigDecimal oupoAmtMin,
                                                     @RequestParam(required = false) @ApiParam(value = "最大案件金额") BigDecimal oupoAmtMax,
                                                     @RequestParam(required = false) @ApiParam(value = "还款状态") String payStatus,
                                                     @RequestParam(required = false) @ApiParam(value = "最小还款金额") BigDecimal oupoPaynumMin,
                                                     @RequestParam(required = false) @ApiParam(value = "最大还款金额") BigDecimal oupoPaynumMax,
                                                     @RequestParam(required = false) @ApiParam(value = "批次号") String outbatch,
                                                     @ApiIgnore Pageable pageable) {
        try{
            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.nonNull(overDayMin)) {
                builder.and(qOutsourcePool.caseInfo.overdueDays.gt(overDayMin));
            }
            if (Objects.nonNull(overDayMax)) {
                builder.and(qOutsourcePool.caseInfo.overdueDays.lt(overDayMax));
            }
            if (Objects.nonNull(outsName)) {
                builder.and(qOutsourcePool.outsource.outsName.eq(outsName));
            }
            if (Objects.nonNull(oupoStatus)) {
                builder.and(qOutsourcePool.caseInfo.collectionStatus.eq(oupoStatus));
            }
            if (Objects.nonNull(oupoAmtMin)) {
                builder.and(qOutsourcePool.caseInfo.overdueAmount.gt(oupoAmtMin));
            }
            if (Objects.nonNull(oupoAmtMax)) {
                builder.and(qOutsourcePool.caseInfo.overdueAmount.lt(oupoAmtMax));
            }
            if (Objects.nonNull(payStatus)) {
                builder.and(qOutsourcePool.caseInfo.payStatus.eq(payStatus));
            }
            if (Objects.nonNull(oupoPaynumMin)) {
                builder.and(qOutsourcePool.caseInfo.hasPayAmount.gt(oupoPaynumMin));
            }
            if (Objects.nonNull(oupoPaynumMax)) {
                builder.and(qOutsourcePool.caseInfo.hasPayAmount.lt(oupoPaynumMax));
            }
            if (Objects.nonNull(outbatch)) {
                builder.and(qOutsourcePool.outBatch.eq(outbatch));
            }
            Page<OutsourcePool> page = outsourcePoolRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/closeOutsourcePool")
    @ApiOperation(value = "委外结案", notes = "委外结案")
    public ResponseEntity<List<OutsourcePool>> closeOutsourcePool(@RequestBody OutCaseIdList outCaseIdList, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try{
            List<String> outCaseIds = outCaseIdList.getOutCaseIds();
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            User user = getUserByToken(token);
            for (String outId:outCaseIds){
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outId);
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDE_OVER.getCode());//状态改为委外结束
                outsourcePool.setOperator(user.getUserName());//委外结案人
                outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());//委外结案时间
                outsourcePools.add(outsourcePool);
            }
            outsourcePools = outsourcePoolRepository.save(outsourcePools);
            return ResponseEntity.ok().body(outsourcePools);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外结案失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/backOutsourcePool")
    @ApiOperation(value = "退案", notes = "退案")
    public ResponseEntity<List<OutsourcePool>> backOutsourcePool(@RequestBody OutCaseIdList outCaseIdList, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try{
            List<String> outCaseIds = outCaseIdList.getOutCaseIds();
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            User user = getUserByToken(token);
            for (String outId:outCaseIds){
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outId);
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());//状态改为待委外
                outsourcePool.setOperator(user.getUserName());//委外退案人
                outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());//委外退案时间
                outsourcePools.add(outsourcePool);
            }
            outsourcePools = outsourcePoolRepository.save(outsourcePools);
            return ResponseEntity.ok().body(outsourcePools);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外退案失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 查询可委外案件
     */
    @GetMapping("/getAllOutCase")
    @ApiOperation(value = "查询可委外案件", notes = "查询可委外案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getAllOutCase(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to get all case");
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //待分配
        list.add(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //待催收
        list.add(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //催收中
        list.add(CaseInfo.CollectionStatus.OVER_PAYING.getValue()); //逾期还款中
        list.add(CaseInfo.CollectionStatus.EARLY_PAYING.getValue()); //提前结清还款中
        list.add(CaseInfo.CollectionStatus.PART_REPAID.getValue()); //部分已还款
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            builder.and(QCaseInfo.caseInfo.collectionStatus.in(list)); //不查询已结案、已还款案件
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/outsourcePoolController/getAllOutCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "caseInfo", e.getMessage())).body(null);
        }
    }
}
