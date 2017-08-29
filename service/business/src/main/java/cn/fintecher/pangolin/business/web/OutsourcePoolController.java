package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.service.AccFinanceEntryService;
import cn.fintecher.pangolin.entity.AccFinanceDataExcel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.CellError;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.*;

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
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    AccFinanceEntryService accFinanceEntryService;
    @Autowired
    AccFinanceEntryRepository accFinanceEntryRepository;
    public static final String FINANCEEXCEL_URL = "http://117.36.75.166:888/group1/M00/00/15/wKgDClmjieeAOeGlAAAmrp2DkmU81.xlsx";
    private static final String ENTITY_NAME = "OutSource";
    private static final String ENTITY_NAME1 = "OutSourcePool";
    private static final String ENTITY_CASEINFO = "CaseInfo";

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
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
                }
                LabelValue seqResult = batchSeqService.nextSeq(CASE_SEQ, 5);
                String ouorBatch = seqResult.getValue();
                for (String cupoId : caseIds) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(cupoId);
                    if (Objects.isNull(caseInfo)){
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "案件不存在")).body(null);
                    }
                    if (CaseInfo.CollectionStatus.CASE_OVER.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已结案案件不能再委外")).body(null);
                    }else if (CaseInfo.CollectionStatus.CASE_OUT.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已委外案件不能再委外")).body(null);
                    }else if (CaseInfo.CollectionStatus.REPAID.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已还款案件不能再委外")).body(null);
                    }
                    //将原案件改为已结案
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OUT.getValue());//已委外
                    caseInfo.setEndType(CaseInfo.EndType.OUTSIDE_CLOSED.getValue());//委外结案
                    caseInfo.setOperator(user);
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseInfo.setEndRemark("委外结案");//结案说明
                    caseInfos.add(caseInfo);
                    Outsource outsource = outsourceRepository.findOne(outsourceInfo.getOutsId());
                    //委外记录
                    OutsourceRecord outsourceRecord = new OutsourceRecord();
                    outsourceRecord.setCaseInfo(caseInfo);
                    outsourceRecord.setOutsource(outsource);
                    outsourceRecord.setCreateTime(ZWDateUtil.getNowDateTime());
                    outsourceRecord.setCreator(user.getUserName());
                    outsourceRecord.setFlag(0);//默认正常
                    outsourceRecord.setOuorBatch(ouorBatch);//批次号
                    outsourceRecords.add(outsourceRecord);
                    //保存委外案件
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setOutsource(outsource);
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setOperator(user.getUserName());
                    outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());
                    outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDING.getCode());//委外中
                    outsourcePool.setOutBatch(ouorBatch);
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
    @GetMapping("/query")
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
                                                     @RequestParam(required = false) String companyCode,
                                                     @RequestHeader(value = "X-UserToken") String token,
                                                     @ApiIgnore Pageable pageable) {
        try{
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutSourcePool", "请选择公司")).body(null);
                }
                builder.and(qOutsourcePool.caseInfo.companyCode.eq(companyCode));
            } else {
                builder.and(qOutsourcePool.caseInfo.companyCode.eq(user.getCompanyCode())); //限制公司code码
            }
            if (Objects.nonNull(overDayMin)) {
                builder.and(qOutsourcePool.caseInfo.overdueDays.gt(overDayMin));
            }
            if (Objects.nonNull(overDayMax)) {
                builder.and(qOutsourcePool.caseInfo.overdueDays.lt(overDayMax));
            }
            if (Objects.nonNull(outsName)) {
                builder.and(qOutsourcePool.outsource.outsName.like("%"+outsName+"%"));
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
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
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
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
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

    @RequestMapping(value = "/exportAccOutsourcePool", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "委外案件导出", notes = "委外案件导出")
    //多条件查询领取案件
    public ResponseEntity<String> getAccOutsourcePoolByToken(@RequestBody OurBatchList ourBatchList,
                                                             @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(ourBatchList.getCompanyCode())) {
                   return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutSourcePool", "请选择公司")).body(null);
                }
                builder.and(qOutsourcePool.caseInfo.companyCode.eq(ourBatchList.getCompanyCode()));
            } else {
                builder.and(qOutsourcePool.caseInfo.companyCode.eq(user.getCompanyCode())); //限制公司code码
            }
            List<String> list = ourBatchList.getOurBatchList();
            if (!list.isEmpty()){
                builder.and(qOutsourcePool.outBatch.in(list));
            }
            List<OutsourcePool> outsourcePools = (List<OutsourcePool>)outsourcePoolRepository.findAll(builder);
            List<AccOutsourcePoolModel> accOutsourcePoolModels = new ArrayList<>();
            for (OutsourcePool outsourcePool:outsourcePools){
                AccOutsourcePoolModel accOutsourcePoolModel = new AccOutsourcePoolModel();
                CaseInfo caseInfo = outsourcePool.getCaseInfo();
                accOutsourcePoolModel.setCaseCode(caseInfo.getCaseNumber());
                if (Objects.nonNull(caseInfo)) {
                    if (Objects.nonNull(caseInfo.getCommissionRate())) {
                        accOutsourcePoolModel.setCommissionRate(caseInfo.getCommissionRate().toString());
                    }
                    if (Objects.nonNull(caseInfo.getContractAmount())) {
                        accOutsourcePoolModel.setContractAmount(caseInfo.getContractAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (Objects.nonNull(caseInfo.getPerPayAmount())) {
                        accOutsourcePoolModel.setCurrentAmount(caseInfo.getPerPayAmount().toString());//每期还款金额
                    }
                    accOutsourcePoolModel.setCurrentPayDate(ZWDateUtil.fomratterDate(caseInfo.getPerDueDate(),"yyyy-MM-dd"));//每期还款日
                    if (Objects.nonNull(caseInfo.getPersonalInfo())) {
                        accOutsourcePoolModel.setCustName(caseInfo.getPersonalInfo().getName());
                    }
                    Set set = caseInfo.getPersonalInfo().getPersonalJobs();
                    PersonalJob personalJob = null;
                    if (!set.isEmpty()){
                        personalJob = (PersonalJob)set.iterator().next();
                    }

                    if (Objects.nonNull(personalJob)){
                        accOutsourcePoolModel.setEmployerAddress(personalJob.getAddress());
                        accOutsourcePoolModel.setEmployerName(personalJob.getCompanyName());
                        accOutsourcePoolModel.setEmployerPhone(personalJob.getPhone());
                    }
                    if (Objects.nonNull(caseInfo.getHasPayAmount())) {
                        accOutsourcePoolModel.setHasPayAmount(caseInfo.getHasPayAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (Objects.nonNull(caseInfo.getHasPayPeriods())) {
                        accOutsourcePoolModel.setHasPayPeriods(caseInfo.getHasPayPeriods().toString());
                    }
                    accOutsourcePoolModel.setHomeAddress(caseInfo.getPersonalInfo().getLocalHomeAddress());
                    accOutsourcePoolModel.setIdCardNumber(caseInfo.getPersonalInfo().getIdCard());
                    if (Objects.nonNull(caseInfo.getLatelyPayAmount())) {
                        accOutsourcePoolModel.setLastPayAmount(caseInfo.getLatelyPayAmount().toString());//最近还款金额
                    }
                    if (Objects.nonNull(caseInfo.getLatelyPayDate())) {
                        accOutsourcePoolModel.setLastPayDate(ZWDateUtil.fomratterDate(caseInfo.getLatelyPayDate(),"yyyy-MM-dd"));//最近还款日期
                    }
                    if (Objects.nonNull(caseInfo.getOverdueAmount())) {
                        accOutsourcePoolModel.setOverDueAmount(caseInfo.getOverdueAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (Objects.nonNull(caseInfo.getOverdueCapital())) {
                        accOutsourcePoolModel.setOverDueCapital(caseInfo.getOverdueCapital().toString());//逾期本金
                    }
                    if (Objects.nonNull(caseInfo.getOverdueDays())) {
                        accOutsourcePoolModel.setOverDueDays(caseInfo.getOverdueDays().toString());
                    }
                    if (Objects.nonNull(caseInfo.getOverdueFine())) {
                        accOutsourcePoolModel.setOverDueDisincentive(caseInfo.getOverdueFine().toString());//逾期罚息
                    }
                    if (Objects.nonNull(caseInfo.getOverdueDelayFine())) {
                        accOutsourcePoolModel.setOverDueFine(caseInfo.getOverdueDelayFine().toString());//逾期滞纳金
                    }
                    if (Objects.nonNull(caseInfo.getOverdueInterest())) {
                        accOutsourcePoolModel.setOverDueInterest(caseInfo.getOverdueInterest().toString());//逾期利息
                    }
                    if (Objects.nonNull(caseInfo.getOverduePeriods())) {
                        accOutsourcePoolModel.setOverDuePeriods(caseInfo.getOverduePeriods().toString());//逾期期数
                    }
                    if (Objects.nonNull(caseInfo.getPersonalInfo())) {
                        accOutsourcePoolModel.setPhoneNumber(caseInfo.getPersonalInfo().getMobileNo());
                    }
                    if (Objects.nonNull(caseInfo.getProduct())) {
                        if (Objects.nonNull(caseInfo.getProduct().getProductSeries())) {
                            accOutsourcePoolModel.setProductSeries(caseInfo.getProduct().getProductSeries().getSeriesName());
                        }
                    }
                    if (Objects.nonNull(caseInfo.getProduct())) {
                        accOutsourcePoolModel.setProductName(caseInfo.getProduct().getProdcutName());
                    }
                    if (Objects.nonNull(caseInfo.getPeriods())) {
                        accOutsourcePoolModel.setPayPeriods(caseInfo.getPeriods().toString());//还款总期数
                    }
                }
                accOutsourcePoolModels.add(accOutsourcePoolModel);
            }
            if (null == accOutsourcePoolModels || accOutsourcePoolModels.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("没有委外数据", "", "没有委外数据")).body(null);
            }

            String[] titleList = {"案件编号", "客户姓名", "身份证号", "手机号码", "产品系列", "产品名称", "合同金额（元）", "逾期总金额（元）", "逾期本金（元）", "逾期利息（元）", "逾期罚息（元）", "逾期滞纳金（元）", "还款期数","逾期期数","逾期天数","已还款金额（元）","已还款期数","最近还款日期","最近还款金额（元）","家庭住址","工作单位名称","工作单位地址","工作单位电话","佣金比例（%）","每期还款日","每期还款金额（元）"};
            String[] proNames = {"caseCode", "custName", "idCardNumber", "phoneNumber", "productSeries", "productName", "contractAmount", "overDueAmount", "overDueCapital", "overDueInterest", "overDueDisincentive", "overDueFine", "payPeriods","overDuePeriods","overDueDays","hasPayAmount","hasPayPeriods","lastPayDate","lastPayAmount","homeAddress","employerName","employerAddress","employerPhone","commissionRate","currentPayDate","currentAmount"};
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            ExcelUtil.createExcel(workbook, sheet, accOutsourcePoolModels, titleList, proNames, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "催收员业绩报表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            String body = url.getBody();
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("上传服务器失败", "", "上传服务器失败")).body(null);
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功","OutsourcePoolController")).body(body);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("导出失败", ENTITY_NAME1, e.getMessage())).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.deleteOnExit();
            }
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
                                                        @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
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
            if (Objects.isNull(tokenUser.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "请选择公司")).body(null);
                }
                builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            } else {
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            }
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

    @GetMapping("/loadTemplate")
    @ResponseBody
    @ApiOperation(value = "下载模板", notes = "下载模板")
    public ResponseEntity<String> loadTemplate(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            return ResponseEntity.ok().body(FINANCEEXCEL_URL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("下载失败", "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/importFinancData")
    @ResponseBody
    @ApiOperation(value = "账目导入", notes = "账目导入")
    public ResponseEntity<List> importExcelData(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                @RequestParam(required = false) @ApiParam(value = "文件ID") String fileId,
                                                @RequestParam(required = false) @ApiParam(value = "备注") String fienRemark) {
        try {
            int[] startRow = {0};
            int[] startCol = {0};
            Class<?>[] dataClass = {AccFinanceDataExcel.class};
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            AccFinanceEntry accFinanceEntry = new AccFinanceEntry();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("AccFinanceEntry", "AccFinanceEntry", "请选择公司")).body(null);
                }
                accFinanceEntry.setCompanyCode(companyCode);
            } else {
                accFinanceEntry.setCompanyCode(user.getCompanyCode());//限制公司code码
            }
            accFinanceEntry.setCreateTime(ZWDateUtil.getNowDateTime());
            accFinanceEntry.setCreator(user.getUserName());
            accFinanceEntry.setFienRemark(fienRemark);
            //查找上传文件
            ResponseEntity<UploadFile> uploadFileResult = null;
            UploadFile uploadFile = null;
            try {
                uploadFileResult = restTemplate.getForEntity("http://file-service/api/uploadFile/" + fileId, UploadFile.class);
                if (!uploadFileResult.hasBody()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取上传文件失败", "", "获取上传文件失败")).body(null);
                } else {
                    uploadFile = uploadFileResult.getBody();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取上传文件失败", "", e.getMessage())).body(null);
            }
            //解析Excel并保存到临时表中
            List<CellError> errorList = accFinanceEntryService.importAccFinanceData(uploadFile.getLocalUrl(), startRow, startCol, dataClass, accFinanceEntry);
            if (errorList.isEmpty()) {
                return ResponseEntity.ok().body(null);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Excel数据有误", "", "Excel数据有误")).body(errorList);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("导入失败", "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/affirmReconciliation")
    @ResponseBody
    @ApiOperation(value = "财务数据确认操作", notes = "财务数据确认操作")
    public ResponseEntity<List> affirmReconciliation(@RequestBody FienCasenums fienCasenums) {
        try {
            if (fienCasenums.getIdList().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("没有可确认的数据", "", "没有可确认的数据")).body(null);
            }
            List<AccFinanceEntry> accFinanceEntryList = new ArrayList<>();
            List<CaseInfo> caseInfoList = new ArrayList<>();  //在委外池中能匹配上的委外案件
            List<AccFinanceEntry> unableMatchList = new ArrayList<>();  //在委外池中没有匹配的财务数据
            List<AccFinanceEntry> accFinanceEntrieAll = accFinanceEntryRepository.findAll(fienCasenums.getIdList());
            for (AccFinanceEntry financeEntryCase : accFinanceEntrieAll) {
                String caseNum = financeEntryCase.getFienCasenum();
                List<CaseInfo> caseInfos = caseInfoRepository.findByCaseNumber(caseNum);
                if (Objects.nonNull(caseInfos) && !caseInfos.isEmpty()) {
                    //对委外客户池已还款金额做累加
                    for (CaseInfo caseInfo:caseInfos){
                        if (Objects.isNull(caseInfo.getHasPayAmount())) {
                            caseInfo.setHasPayAmount(new BigDecimal(0));
                        }
                        caseInfo.setHasPayAmount(caseInfo.getHasPayAmount().add(financeEntryCase.getFienPayback()));
                        caseInfoList.add(caseInfo);
                    }

                }else {
                    unableMatchList.add(financeEntryCase);   //未有匹配委外案件
                }
                //临时表中的数据状态为已确认。
                financeEntryCase.setFienStatus(Status.Disable.getValue());
                accFinanceEntryList.add(financeEntryCase);
            }
            //同步更新临时表中的数据状态为已确认
            accFinanceEntryRepository.save(accFinanceEntryList);
            //更新原有的案件案件金额
            caseInfoRepository.save(caseInfoList);
            return ResponseEntity.ok().body(unableMatchList);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("确认失败", "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/findFinanceData")
    @ResponseBody
    @ApiOperation(value = "查询未确认的数据", notes = "查询未确认的数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<AccFinanceEntry>> findFinanceData(@ApiIgnore Pageable pageable,
                                                                 @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                 @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            AccFinanceEntry accFinanceEntry = new AccFinanceEntry();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("AccFinanceEntry", "AccFinanceEntry", "请选择公司")).body(null);
                }
                accFinanceEntry.setCompanyCode(companyCode);
            } else {
                accFinanceEntry.setCompanyCode(user.getCompanyCode()); //限制公司code码
            }
            accFinanceEntry.setFienStatus(Status.Enable.getValue());
            accFinanceEntry.setFienCount(null);
            accFinanceEntry.setFienPayback(null);
            ExampleMatcher matcher = ExampleMatcher.matching();
            org.springframework.data.domain.Example<AccFinanceEntry> example = org.springframework.data.domain.Example.of(accFinanceEntry, matcher);
            Page<AccFinanceEntry> page = accFinanceEntryRepository.findAll(example, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "", e.getMessage())).body(null);
        }
    }


    @PostMapping("/deleteFinanceData")
    @ResponseBody
    @ApiOperation(value = "财务数据删除操作", notes = "财务数据删除操作")
    public ResponseEntity deleteFinanceData(@RequestBody FienCasenums fienCasenums) {
        try {
            if(fienCasenums.getIdList().isEmpty()){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("没有可删除的数据", "", "没有可删除的数据")).body(null);
            }
            for(String id : fienCasenums.getIdList()){
                accFinanceEntryRepository.delete(id);
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("删除失败", "", e.getMessage())).body(null);
        }
    }


    @RequestMapping(value = "/exportOutsideFinanceData", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "导出委外财务对账数据", notes = "导出委外财务对账数据")
    public ResponseEntity<String> exportOutsideFinanceData(@RequestParam(value = "oupoOutbatch", required = false) @ApiParam("批次号") String oupoOutbatch,
                                                           @RequestParam(value = "outsName", required = false) @ApiParam("委外方") String outsName,
                                                           @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        try {
            List<OutsourcePool> accOutsourcePoolList;
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            try {
                QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
                BooleanBuilder builder = new BooleanBuilder();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (Objects.isNull(companyCode)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutsourcePool", "请选择公司")).body(null);
                    }
                    builder.and(qOutsourcePool.caseInfo.companyCode.eq(companyCode));
                } else {
                    builder.and(qOutsourcePool.caseInfo.companyCode.eq(user.getCompanyCode())); //限制公司code码
                }
                if (Objects.nonNull(oupoOutbatch)) {
                    builder.and(qOutsourcePool.outBatch.gt(oupoOutbatch));
                }
                if (Objects.nonNull(outsName)) {
                    builder.and(qOutsourcePool.outsource.outsName.like("%"+outsName+"%"));
                }
                accOutsourcePoolList = (List<OutsourcePool>)outsourcePoolRepository.findAll(builder);
            } catch (Exception e) {
                e.getStackTrace();
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询委外案件失败", "", e.getMessage())).body(null);
            }
            // 按照条件得到的财务数据为空时不允许导出
            if (accOutsourcePoolList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("要导出的财务数据为空", "", "要导出的财务数据为空")).body(null);
            }
            // 将需要的数据获取到按照导出的模板存放在List中
            List<AccOutsideFinExportModel> accOutsideList = new ArrayList<>();
            for (int i = 0; i < accOutsourcePoolList.size(); i++) {
                OutsourcePool aop = accOutsourcePoolList.get(i);
                AccOutsideFinExportModel expm = new AccOutsideFinExportModel();
                expm.setOupoOutbatch(checkValueIsNull(aop.getOutBatch())); // 委外批次号
                expm.setOupoCasenum(checkValueIsNull(aop.getCaseInfo().getCaseNumber())); // 案件编号
                expm.setCustName(checkValueIsNull(aop.getCaseInfo().getPersonalInfo().getName()));  // 客户名称
                expm.setOupoIdcard(checkValueIsNull(aop.getCaseInfo().getPersonalInfo().getIdCard()));  // 身份证号
                expm.setOupoStatus(checkOupoStatus(aop.getOutStatus())); // 委外状态
                expm.setOupoAmt(checkValueIsNull(aop.getCaseInfo().getOverdueAmount()));  // 案件金额
                expm.setOupoPaynum(checkValueIsNull(aop.getCaseInfo().getHasPayAmount())); // 已还款金额
                expm.setOutsName(aop.getOutsource().getOutsName());  // 委外方名称
                accOutsideList.add(expm);
            }

            // 将存放的数据写入Excel
            String[] titleList = {"案件编号", "客户姓名", "客户身份证号", "委外状态", "委外方", "案件金额", "已还款金额"};
            String[] proNames = {"oupoCasenum", "custName", "oupoIdcard", "oupoStatus", "outsName", "oupoAmt", "oupoPaynum"};
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            ExcelUtil.createExcel(workbook, sheet, accOutsideList, titleList, proNames, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "财务数据对账.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("上传文件服务器失败", "", "上传文件服务器失败")).body(null);
            } else {
                return ResponseEntity.ok().body(url.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("上传文件服务器失败", "", e.getMessage())).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }

    /**
     * 检查值，为空时转为空字符串，不为空统一转为字符串
     */
    private String checkValueIsNull(Object obj) {
        if (Objects.nonNull(obj)) {
            return String.valueOf(obj.equals("null") ? "" : obj);
        } else {
            return null;
        }
    }

    /**
     * 将接受到的数字转换成相应的字符串
     */
    private String checkOupoStatus(Object obj) {
        if (Objects.nonNull(obj)) {
            if (Objects.equals(OutsourcePool.OutStatus.TO_OUTSIDE.getCode(), obj)) {
                return "待委外";
            } else if (Objects.equals(OutsourcePool.OutStatus.OUTSIDING.getCode(), obj)) {
                return "委外中";
            } else if (Objects.equals(OutsourcePool.OutStatus.OUTSIDE_EXPIRE.getCode(), obj)) {
                return "委外到期";
            } else {
                return "委外结束";
            }
        } else {
            return null;
        }
    }

    @GetMapping("/getOutRecored")
    @ApiOperation(value = "查询委外记录", notes = "查询委外记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutsourceRecord>> getAccOutsourceOrder(
            @RequestParam(required = false) @ApiParam(value = "委外方") String outsName,
            @RequestParam(required = false) @ApiParam(value = "开始时间") String startDate,
            @RequestParam(required = false) @ApiParam(value = "结束时间") String endDate,
            @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
            @RequestHeader(value = "X-UserToken") String token,
            Pageable pageable) throws URISyntaxException {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            if (StringUtils.isNotBlank(startDate)) {
                startDate = startDate + " 00:00:00";
            }
            if (StringUtils.isNotBlank(endDate)) {
                endDate = endDate + " 23:59:59";
            }
            Date minTime = ZWDateUtil.getUtilDate(startDate, "yyyy-MM-dd HH:mm:ss");
            Date maxTime = ZWDateUtil.getUtilDate(endDate, "yyyy-MM-dd HH:mm:ss");
            QOutsourceRecord qOutsourceRecord = QOutsourceRecord.outsourceRecord;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourceRecord", "OutsourceRecord", "请选择公司")).body(null);
                }
                builder.and(qOutsourceRecord.caseInfo.companyCode.eq(companyCode));
            } else {
                builder.and(qOutsourceRecord.caseInfo.companyCode.eq(user.getCompanyCode())); //限制公司code码
            }
            if (Objects.nonNull(startDate)) {
                builder.and(qOutsourceRecord.createTime.gt(minTime));
            }
            if (Objects.nonNull(endDate)) {
                builder.and(qOutsourceRecord.createTime.lt(maxTime));
            }
            if (Objects.nonNull(outsName)) {
                builder.and(qOutsourceRecord.outsource.outsName.eq(outsName));
            }
            Page<OutsourceRecord> page = outsourceRecordRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "", e.getMessage())).body(null);
        }
    }
}
