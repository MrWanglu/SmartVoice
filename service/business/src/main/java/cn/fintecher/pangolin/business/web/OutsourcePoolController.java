package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.AccFinanceEntryService;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
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
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    private Configuration freemarkerConfiguration;
    @Inject
    private PersonalRepository personalRepository;
    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;
    @Inject
    private CaseInfoVerificationApplyRepository caseInfoVerificationApplyRepository;

    private static final String ENTITY_NAME = "OutSource";
    private static final String ENTITY_NAME1 = "OutSourcePool";
    private static final String ENTITY_CASEINFO = "CaseInfo";

    @PostMapping("/outsource")
    @ApiOperation(value = "委外案件手动分配", notes = "委外案件手动分配")
    public ResponseEntity<Void> batchDistribution(@RequestBody OutsourceInfo outsourceInfo, @RequestHeader(value = "X-UserToken") String token) {
        try {
            List<String> outCaseIds = outsourceInfo.getOutCaseIds();//待委外的案件id集合
            if (outCaseIds.isEmpty()){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePool", "", "未选择案件")).body(null);
            }
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            for (String outCaseId : outCaseIds){//获取未分配的案件信息
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outCaseId);
                if (Objects.isNull(outsourcePool)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePool", "", "案件查询异常")).body(null);
                }
                outsourcePools.add(outsourcePool);
            }
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();//用于批量保存已分配出去案件的空盒子
            List<OutDistributeParam> outDistributes =  outsourceInfo.getOutDistributes();//委外分配信息
            if (outDistributes.isEmpty()){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Outsource", "", "未选择委外方")).body(null);
            }
            Integer rule =  outsourceInfo.getRule();//分配规则
            List<OutsourceRecord> outsourceRecords = new ArrayList<>();//待保存的案件委外记录集合
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            LabelValue seqResult = batchSeqService.nextSeq(CASE_SEQ, 5);
            String ouorBatch = seqResult.getValue();
                if (1 == rule){//优先共債案件
                    Map<String,Integer> map = new HashMap();
                    for (int i=0;i<outsourcePools.size();i++) {//遍历所有所选案件以便查询所有共债案件
                        OutsourcePool outsourcePool = outsourcePools.get(i);
                        if (Objects.isNull(outsourcePool) || Objects.isNull(outsourcePool.getCaseInfo())){
                            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfo", "", "案件查询异常")).body(null);
                        }
                        String custName = outsourcePool.getCaseInfo().getPersonalInfo().getName();
                        if (Objects.isNull(custName)){
                            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalInfo", "", "客户名称查询异常")).body(null);
                        }
                        String idCard = outsourcePool.getCaseInfo().getPersonalInfo().getIdCard();
                        if (Objects.isNull(idCard)){
                            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalInfo", "", "客户身份证号查询异常")).body(null);
                        }
                        Object[] nums = outsourcePoolRepository.getGzNum(custName,idCard);
                        if (Objects.nonNull(nums) && nums.length > 0){//
                            int lastNum = 0;//上一委外方的案件共债数
                            String lastOutId = null;//上一委外方id
                            for (int j=0;j<nums.length;j++){
                                Object[] object = (Object[]) nums[j];
                                int num = ((BigInteger)object[1]).intValue();//案件共债数
                                String outId = (String)object[0];//委外方id
                                if (j != 0){
                                    if (lastNum <= num){//本次>上次
                                        lastNum = num;
                                        lastOutId = outId;
                                    }
                                } else {
                                    lastNum = num;
                                    lastOutId = outId;
                                }
                            }

                            Outsource outsource = outsourceRepository.findOne(lastOutId);
                            if (Objects.isNull(outsource)){
                                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Outsource", "", "委外方查询异常")).body(null);
                            }
                            //优先将案件委外给有共债案件的委外方
                            setOutsourcePool(outsourcePool,outsource,ouorBatch,user,outsourcePoolList);
                            outsourcePools.remove(0);//干掉已经分出去的案件
                            //添加委外记录
                            saveOutsourceRecord(outsourcePool,outsource,user,ouorBatch,outsourceRecords);

                            i--;//如果有删除则向前补一位
                            //记录已经分配的委外方及分配数
                            if (map.containsKey(lastOutId)) {
                                map.put(lastOutId,map.get(lastOutId)+1);
                            } else {
                                map.put(lastOutId,1);
                            }
                        }
                    }

                    //共债剩余未分配案件按手动输入个数依次填满，共债分配数大于等于手动输入个数的则不再分配
                    for (OutDistributeParam outDistributeParam:outDistributes){
                        String outId = outDistributeParam.getOutId();
                        int distributionCount = outDistributeParam.getDistributionCount();//应分配案件数
                        int alreadyDistributionCount = map.get(outId);//已分配案件数
                        if (Objects.nonNull(map.get(outId))){
                            outDistributeParam.setDistributionCount(distributionCount-alreadyDistributionCount);
                        }
                        distributionCount = outDistributeParam.getDistributionCount();//共债分完后还可分配的案件数
                        while (distributionCount > 0){
                            OutsourcePool outsourcePool = outsourcePools.get(0);
                            Outsource outsource = outsourceRepository.findOne(outId);
                            if (Objects.isNull(outsource)){
                                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Outsource", "", "委外方查询异常")).body(null);
                            }
                            setOutsourcePool(outsourcePool,outsource,ouorBatch,user,outsourcePoolList);
                            //添加委外记录
                            saveOutsourceRecord(outsourcePool,outsource,user,ouorBatch,outsourceRecords);
                            distributionCount--;//分配数-1
                            outsourcePools.remove(0);//添加完后删除案件集合中的所分案件
                        }
                    }
                } else if (2==rule){//案件数量平均分法

                    if (outCaseIds.size() > outDistributes.size()){//案件数大于委外方数
                        int avgNum = outCaseIds.size() / outDistributes.size();//平均数
                        //先给每个委外方分配avgNum个案件，剩余的按顺序依次分配
                        for (OutDistributeParam outDistributeParam:outDistributes){
                            String outId = outDistributeParam.getOutId();
                            for (int i=0;i<avgNum;i++){
                                Outsource outsource = outsourceRepository.findOne(outId);
                                OutsourcePool outsourcePool = outsourcePools.get(0);
                                setOutsourcePool(outsourcePool,outsource,ouorBatch,user,outsourcePoolList);
                                outsourcePools.remove(0);
                                //添加委外记录
                                saveOutsourceRecord(outsourcePool,outsource,user,ouorBatch,outsourceRecords);
                            }
                        }
                    }
                    //案件数不足的情况（案件数<=委外方数）
                    for (OutsourcePool outsourcePool:outsourcePools) {
                        String outId = outDistributes.get(0).getOutId();
                        Outsource outsource = outsourceRepository.findOne(outId);
                        setOutsourcePool(outsourcePool,outsource,ouorBatch,user,outsourcePoolList);
                        //每个委外方分到案件后就不再分配
                        outDistributes.remove(0);
                        //添加委外记录
                        saveOutsourceRecord(outsourcePool,outsource,user,ouorBatch,outsourceRecords);
                    }
                } else {//无规则分配(按手动输入案件数分配)
                    for (OutDistributeParam outDistributeParam:outDistributes){
                        String outId = outDistributeParam.getOutId();
                        int distributionCount = outDistributeParam.getDistributionCount();//应分配案件数
                        while (distributionCount > 0){
                            OutsourcePool outsourcePool = outsourcePools.get(0);
                            Outsource outsource = outsourceRepository.findOne(outId);
                            if (Objects.isNull(outsource)){
                                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Outsource", "", "委外方查询异常")).body(null);
                            }
                            setOutsourcePool(outsourcePool,outsource,ouorBatch,user,outsourcePoolList);
                            outsourcePools.remove(0);//添加完后删除案件集合中的所分案件
                            //添加委外记录
                            saveOutsourceRecord(outsourcePool,outsource,user,ouorBatch,outsourceRecords);
                            distributionCount--;//分配数-1
                        }
                    }
                }
           outsourcePoolRepository.save(outsourcePoolList);//批量保存分配的案子
           outsourceRecordRepository.save(outsourceRecords);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外失败", ENTITY_NAME, e.getMessage())).body(null);
        }

    }

    private void saveOutsourceRecord(OutsourcePool outsourcePool, Outsource outsource, User user, String ouorBatch, List<OutsourceRecord> outsourceRecords) {
        //委外记录
        OutsourceRecord outsourceRecord = new OutsourceRecord();
        outsourceRecord.setCaseInfo(outsourcePool.getCaseInfo());
        outsourceRecord.setOutsource(outsource);
        outsourceRecord.setCreateTime(ZWDateUtil.getNowDateTime());
        outsourceRecord.setCreator(user.getUserName());
        outsourceRecord.setFlag(0);//默认正常
        outsourceRecord.setOuorBatch(ouorBatch);//批次号
        outsourceRecords.add(outsourceRecord);
    }


    private void setOutsourcePool(OutsourcePool outsourcePool, Outsource outsource, String ouorBatch, User user, List<OutsourcePool> outsourcePoolList) {
        outsourcePool.setOutsource(outsource);
        outsourcePool.setOutBatch(ouorBatch);
        outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());
        outsourcePool.setOperator(user.getUserName());
        outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDING.getCode());
        outsourcePool.setOutTime(ZWDateUtil.getNowDateTime());
        BigDecimal b2=outsourcePool.getCaseInfo().getHasPayAmount();//已还款金额
        if (Objects.isNull(b2)){
            outsourcePool.getCaseInfo().setHasPayAmount(BigDecimal.ZERO);
        }
        BigDecimal b1=outsourcePool.getCaseInfo().getOverdueAmount();//原案件金额
        outsourcePool.setContractAmt(b1.subtract(b2));//委外案件金额=原案件金额-已还款金额
        outsourcePool.setOverduePeriods(outsourcePool.getOverduePeriods());//逾期时段
        GregorianCalendar gc=new GregorianCalendar();
        gc.setTime(ZWDateUtil.getNowDateTime());
        gc.add(2,3);
        outsourcePool.setOverOutsourceTime(gc.getTime());
        outsourcePoolList.add(outsourcePool);
    }

    /**
     * @Description : 查询委外分配信息
     */
    @PostMapping("/getOutDistributeInfo")
    @ApiOperation(value = "查询委外分配信息", notes = "查询委外分配信息")
    public ResponseEntity<Page<OutDistributeInfo>> query(@RequestBody OutCodeList outCodeList,
                                                         @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            BooleanBuilder builder = new BooleanBuilder();
            String companyCode = outCodeList.getCompanyCode();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutSourcePool", "请选择公司")).body(null);
                }
            } else {
                companyCode = user.getCompanyCode();
            }
            List<OutDistributeInfo> outDistributeInfos = new ArrayList<>();
            Object[] object = outsourcePoolRepository.getOutDistributeInfo(companyCode);
            for (int i=0;i<object.length;i++){
                Object[] object1 = (Object[]) object[i];
                if(Objects.nonNull(object1[1])){
                    String outCode = (String) object1[1];
                    String outName = (String) object1[2];
                    Integer sumNum = ((BigInteger) object1[3]).intValue();
                    Integer endNum = ((BigInteger) object1[4]).intValue();
                    BigDecimal sumAmt = (BigDecimal) object1[5];
                    BigDecimal endAmt = (BigDecimal) object1[6];
                    Integer collectionNum = ((BigInteger) object1[7]).intValue();
                    BigDecimal collectionAmt = (BigDecimal) object1[8];
                    OutDistributeInfo outDistributeInfo = new OutDistributeInfo();
                    outDistributeInfo.setOutCode(outCode);
                    outDistributeInfo.setOutName(outName);
                    outDistributeInfo.setCaseCount(sumNum);
                    outDistributeInfo.setEndCount(endNum);
                    outDistributeInfo.setCaseAmt(sumAmt);
                    outDistributeInfo.setEndAmt(endAmt);
                    outDistributeInfo.setCollectionCount(collectionNum);
                    outDistributeInfo.setCollectionAmt(collectionAmt);
                    outDistributeInfos.add(outDistributeInfo);
                }

            }
            if(Objects.nonNull(outCodeList.getOutCode())){
                List<OutDistributeInfo> outDistributeInfos1 = new ArrayList<>();//存储选择的委外方
                for(OutDistributeInfo out: outDistributeInfos){
                   for(String outcode: outCodeList.getOutCode()){
                       if(outcode.equals(out.getOutCode())){
                           outDistributeInfos1.add(out);
                       }
                   }

                }
                Page<OutDistributeInfo> page1 = new PageImpl(outDistributeInfos1);
                return ResponseEntity.ok().body(page1);
            }
            Page<OutDistributeInfo> page = new PageImpl(outDistributeInfos);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @GetMapping("/outCaseScore")
    @ApiOperation(value = "案件评分(手动)", notes = "案件评分(手动)")
    public ResponseEntity outCaseScore(@RequestParam(required = false) String companyCode,@RequestHeader(value = "X-UserToken") String token) throws IOException {
        try {
            User user = null;
            try {
                user = getUserByToken(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Objects.isNull(user.getCompanyCode())){
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutSourcePool", "请选择公司")).body(null);
                }
            } else {
                companyCode = user.getCompanyCode();
            }
            StopWatch watch1 = new StopWatch();
            watch1.start();
            KieSession kieSession = null;
            try {
                kieSession = createSorceRule(companyCode);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
            Iterable<OutsourcePool> outsourcePools = outsourcePoolRepository.findAll(QOutsourcePool.outsourcePool.outStatus.eq(OutsourcePool.OutStatus.TO_OUTSIDE.getCode())
                    .and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode())));
            List<OutsourcePool> outsourcePoolList = (List<OutsourcePool>)outsourcePools;
            ScoreNumbersModel scoreNumbersModel = new ScoreNumbersModel();
            scoreNumbersModel.setTotal(outsourcePoolList.size());
            if (outsourcePoolList.size() > 0) {
                for (OutsourcePool outsourcePool : outsourcePoolList) {
                    ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                    int age = IdcardUtils.getAgeByIdCard(outsourcePool.getCaseInfo().getPersonalInfo().getIdCard());
                    scoreRuleModel.setAge(age);
                    scoreRuleModel.setOverDueAmount(outsourcePool.getCaseInfo().getOverdueAmount().doubleValue());
                    scoreRuleModel.setOverDueDays(outsourcePool.getCaseInfo().getOverdueDays());
                    scoreRuleModel.setProId(outsourcePool.getCaseInfo().getArea().getId());//省份id
                    Personal personal = personalRepository.findOne(outsourcePool.getCaseInfo().getPersonalInfo().getId());
                    if (Objects.nonNull(personal) && Objects.nonNull(personal.getPersonalJobs())) {
                        scoreRuleModel.setIsWork(1);
                    } else {
                        scoreRuleModel.setIsWork(0);
                    }
                    kieSession.insert(scoreRuleModel);//插入
                    kieSession.fireAllRules();//执行规则
                    outsourcePool.getCaseInfo().setScore(new BigDecimal(scoreRuleModel.getCupoScore()));
                }
                kieSession.dispose();
                outsourcePoolRepository.save(outsourcePoolList);
                watch1.stop();
                log.info("耗时：" + watch1.getTotalTimeMillis());
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分完成", "success")).body(scoreNumbersModel);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseinfo", "failure", "案件为空")).body(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "上传文件服务器失败")).body(null);
        }
    }

    /**
     * 动态生成规则
     *
     * @return
     * @throws IOException
     * @throws
     */
    private KieSession createSorceRule(String comanyCode) throws IOException, TemplateException {
        freemarker.template.Template scoreFormulaTemplate = freemarkerConfiguration.getTemplate("scoreFormula.ftl", "UTF-8");
        freemarker.template.Template scoreRuleTemplate = freemarkerConfiguration.getTemplate("scoreRule.ftl", "UTF-8");
        ResponseEntity<ScoreRules> responseEntity = restTemplate.getForEntity(Constants.SCOREL_SERVICE_URL.concat("getScoreRules").concat("?comanyCode=").concat(comanyCode), ScoreRules.class);
        List<ScoreRule> rules = null;
        if (Objects.nonNull(responseEntity.hasBody())) {
            if (responseEntity.hasBody()) {
                ScoreRules scoreRules = responseEntity.getBody();
                rules = scoreRules.getScoreRules();
            }
        } else {
            throw new IllegalStateException("请设置案件评分策略！");
        }

        StringBuilder sb = new StringBuilder();
        if (Objects.nonNull(rules)) {
            for (ScoreRule rule : rules) {
                for (int i = 0; i < rule.getFormulas().size(); i++) {
                    ScoreFormula scoreFormula = rule.getFormulas().get(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", rule.getId());
                    map.put("index", String.valueOf(i));
                    map.put("strategy", scoreFormula.getStrategy());
                    map.put("score", String.valueOf(scoreFormula.getScore()));
                    map.put("weight", String.valueOf(rule.getWeight()));
                    sb.append(FreeMarkerTemplateUtils.processTemplateIntoString(scoreFormulaTemplate, map));
                }
            }
        }
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        Map<String, String> map = new HashMap<>();
        map.put("allRules", sb.toString());
        String text = FreeMarkerTemplateUtils.processTemplateIntoString(scoreRuleTemplate, map);
        kfs.write("src/main/resources/simple.drl",
                kieServices.getResources().newReaderResource(new StringReader(text)));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieContainer kieContainer =
                kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        return kieSession;
    }

    /**
     * @Description 多条件查询回收案件
     */
    @GetMapping("/getReturnCaseByConditions")
    @ApiOperation(value = "多条件查询回收案件", notes = "多条件查询回收案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoReturn>> getReturnCaseByConditions(@QuerydslPredicate(root = CaseInfoReturn.class) Predicate predicate,
                                                                         @ApiIgnore Pageable pageable,
                                                                          @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                         @RequestHeader(value = "X-UserToken") String token) {
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            QCaseInfoReturn qCaseInfoReturn = QCaseInfoReturn.caseInfoReturn;
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoReturn", "", "请选择公司")).body(null);
                }
            } else {
                builder.and(qCaseInfoReturn.caseId.companyCode.eq(user.getCompanyCode())); //限制公司code码
            }
            builder.and(qCaseInfoReturn.source.eq(CaseInfo.CasePoolType.OUTER.getValue())); //委外
            Page<CaseInfoReturn> page = caseInfoReturnRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/OutsourcePoolController/getReturnCaseByConditions");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoReturn", "", "查询失败")).body(null);
        }
    }

    @PostMapping("/verificationApply")
    @ApiOperation(value = "核销申请", notes = "核销申请")
    public ResponseEntity verificationApply(@RequestBody VerificationApplyModel verificationApplyModel, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            List<String> ids = verificationApplyModel.getIds();
            List<CaseInfoVerificationApply> verificationApplies = new ArrayList<>();
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("User", "", "获取不到登录人信息")).body(null);
            }
            for (String id : ids) {
                CaseInfoReturn caseInfoReturn = caseInfoReturnRepository.findOne(id);
                if (Objects.isNull(caseInfoReturn)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoReturn", "", "获取回收案件信息失败")).body(null);
                }
                CaseInfoVerificationApply caseInfoVerificationApply = new CaseInfoVerificationApply();
                caseInfoVerificationApply.setOperator(user.getRealName()); // 操作人
                caseInfoVerificationApply.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
                caseInfoVerificationApply.setApplicant(user.getRealName()); // 申请人
                caseInfoVerificationApply.setApplicationDate(ZWDateUtil.getNowDateTime()); // 申请日期
                caseInfoVerificationApply.setApplicationReason(verificationApplyModel.getReason()); // 申请理由
                caseInfoVerificationApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue()); // 申请状态：审批待通过
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                if (Objects.nonNull(caseInfo)) {
                    caseInfoVerificationApply.setCaseId(caseInfo.getId()); // 案件Id
                    caseInfoVerificationApply.setCaseNumber(caseInfo.getCaseNumber()); // 案件编号
                    caseInfoVerificationApply.setBatchNumber(caseInfo.getBatchNumber()); // 批次号
                    caseInfoVerificationApply.setOverdueAmount(caseInfo.getOverdueAmount()); // 逾期金额
                    caseInfoVerificationApply.setOverdueDays(caseInfo.getOverdueDays()); // 逾期天数
                    caseInfoVerificationApply.setPayStatus(caseInfo.getPayStatus()); // 还款状态
                    caseInfoVerificationApply.setContractNumber(caseInfo.getContractNumber()); // 合同编号
                    caseInfoVerificationApply.setContractAmount(caseInfo.getContractAmount()); // 合同金额
                    caseInfoVerificationApply.setOverdueCapital(caseInfo.getOverdueCapital()); // 逾期本金
                    caseInfoVerificationApply.setOverdueDelayFine(caseInfo.getOverdueDelayFine()); // 逾期滞纳金
                    caseInfoVerificationApply.setOverdueFine(caseInfo.getOverdueFine()); // 逾期罚息
                    caseInfoVerificationApply.setOverdueInterest(caseInfo.getOverdueInterest()); // 逾期利息
                    caseInfoVerificationApply.setHasPayAmount(caseInfo.getHasPayAmount()); // 已还款金额
                    caseInfoVerificationApply.setHasPayPeriods(caseInfo.getHasPayPeriods()); // 已还款期数
                    caseInfoVerificationApply.setLatelyPayAmount(caseInfo.getLatelyPayAmount()); // 最近还款金额
                    caseInfoVerificationApply.setLatelyPayDate(caseInfo.getLatelyPayDate()); // 最近还款日期
                    caseInfoVerificationApply.setPeriods(caseInfo.getPeriods()); // 还款期数
                    caseInfoVerificationApply.setCompanyCode(caseInfo.getCompanyCode());
                    caseInfoVerificationApply.setCommissionRate(caseInfo.getCommissionRate()); // 佣金比例
                    if (Objects.nonNull(caseInfo.getArea())) {
                        caseInfoVerificationApply.setCity(caseInfo.getArea().getId()); // 城市
                        if (Objects.nonNull(caseInfo.getArea().getParent())) {
                            caseInfoVerificationApply.setProvince(caseInfo.getArea().getParent().getId()); // 省份
                        }
                    }
                    if (Objects.nonNull(caseInfo.getPrincipalId())) {
                        caseInfoVerificationApply.setPrincipalName(caseInfo.getPrincipalId().getName()); // 委托方名称
                    }
                    if (Objects.nonNull(caseInfo.getPersonalInfo())) {
                        caseInfoVerificationApply.setPersonalName(caseInfo.getPersonalInfo().getName()); // 客户名称
                        caseInfoVerificationApply.setMobileNo(caseInfo.getPersonalInfo().getMobileNo()); // 电话号
                        caseInfoVerificationApply.setIdCard(caseInfo.getPersonalInfo().getIdCard()); // 身份证号
                    }
                }
                verificationApplies.add(caseInfoVerificationApply);
            }
            caseInfoVerificationApplyRepository.save(verificationApplies);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("核销申请失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/changeToBeAssigned")
    @ApiOperation(value = "移入待分配", notes = "移入待分配")
    public ResponseEntity changeToBeAssigned(@RequestBody VerificationApplyModel verificationApplyModel, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            List<String> ids = verificationApplyModel.getIds();
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("User", "", "获取不到登录人信息")).body(null);
            }
            for (String id : ids) {
                CaseInfoReturn caseInfoReturn = caseInfoReturnRepository.findOne(id);
                if (Objects.isNull(caseInfoReturn)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoReturn", "", "获取回收案件信息失败")).body(null);
                }
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                caseInfo.setRecoverWay(CaseInfo.RecoverWay.MANUAL.getValue());//默认手动回收
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue());//未回收
                caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());//流入时间
                OutsourcePool outsourcePool = new OutsourcePool();
                outsourcePool.setCaseInfo(caseInfo);
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
                outsourcePoolRepository.save(outsourcePool);
                caseInfoReturnRepository.delete(id);//删除原回收案件
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("核销申请失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }


    /**
     * @Description : 查询委外案件
     */
    @GetMapping("/query")
    @ApiOperation(value = "查询待分配委外案件", notes = "查询待分配委外案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutsourcePool>> query(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                     @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                     @ApiIgnore Pageable pageable,
                                                     @RequestHeader(value = "X-UserToken") String token,
                                                     @RequestParam @ApiParam(value = "tab页标识 1待分配;2已结案", required = true) Integer flag) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }

            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutSourcePool", "请选择公司")).body(null);
                }
            } else {
                builder.and(qOutsourcePool.caseInfo.companyCode.eq(user.getCompanyCode())); //限制公司code码
            }
            builder.and(qOutsourcePool.caseInfo.casePoolType.eq(CaseInfo.CasePoolType.OUTER.getValue()));//委外类型
            if (1 == flag){
                builder.and(qOutsourcePool.outStatus.eq(OutsourcePool.OutStatus.TO_OUTSIDE.getCode())); //待分配
            }else if (2 == flag){
                builder.and(qOutsourcePool.outStatus.eq(OutsourcePool.OutStatus.OUTSIDE_OVER.getCode())); //已结案
            }
            Page<OutsourcePool> page = outsourcePoolRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/recallOutCase")
    @ApiOperation(value = "撤回", notes = "撤回")
    public ResponseEntity recallOutCase(@RequestBody VerificationApplyModel verificationApplyModel, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            List<String> ids = verificationApplyModel.getIds();
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("User", "", "获取不到登录人信息")).body(null);
            }
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            for (String id : ids) {
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(id);
                if (Objects.isNull(outsourcePool)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePool", "", "案件查询异常")).body(null);
                }
                Date outTime = outsourcePool.getOutTime();
                if (Objects.isNull(outTime)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePool", "", "获取委外时间异常")).body(null);
                }
                Integer days = ZWDateUtil.getBetween(outTime,ZWDateUtil.getNowDateTime(), ChronoUnit.DAYS);
                if (days >= 3){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePool", "", "委外已超过3天，不能撤回")).body(null);
                }
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
                outsourcePool.setContractAmt(null);
                outsourcePool.setOverduePeriods(null);
                outsourcePool.setEndOutsourceTime(null);
                outsourcePool.setOverOutsourceTime(null);
                outsourcePool.setOutsource(null);
                outsourcePool.setOperateTime(null);
                outsourcePool.setOperator(null);
                outsourcePool.setOutBatch(null);
                outsourcePool.setOutBackAmt(null);
                outsourcePool.setOutTime(null);
                outsourcePool.setOutoperationStatus(null);
                outsourcePools.add(outsourcePool);
            }
            outsourcePoolRepository.save(outsourcePools);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("核销申请失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/closeOutsourcePool")
    @ApiOperation(value = "委外结案", notes = "委外结案")
    public ResponseEntity<List<OutsourcePool>> closeOutsourcePool(@RequestBody OutCaseIdList outCaseIdList, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            List<String> outCaseIds = outCaseIdList.getOutCaseIds();
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            for (String outId : outCaseIds) {
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outId);
                if (OutsourcePool.OutStatus.OUTSIDE_OVER.getCode().equals(outsourcePool.getOutStatus())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("已委外结案案件不能再结案", "", "已委外结案案件不能再结案")).body(null);
                }
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDE_OVER.getCode());//状态改为委外结束
                outsourcePool.setOperator(user.getUserName());//委外结案人
                outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());//委外结案时间
                outsourcePools.add(outsourcePool);
            }
            outsourcePools = outsourcePoolRepository.save(outsourcePools);
            return ResponseEntity.ok().body(outsourcePools);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外结案失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

    @PostMapping("/backOutsourcePool")
    @ApiOperation(value = "退案", notes = "退案")
    public ResponseEntity<List<OutsourcePool>> backOutsourcePool(@RequestBody OutCaseIdList outCaseIdList, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            List<String> outCaseIds = outCaseIdList.getOutCaseIds();
            List<OutsourcePool> outsourcePools = new ArrayList<>();
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            for (String outId : outCaseIds) {
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outId);
                outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());//状态改为待委外
                outsourcePool.setOperator(user.getUserName());//委外退案人
                outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());//委外退案时间
                outsourcePools.add(outsourcePool);
            }
            outsourcePools = outsourcePoolRepository.save(outsourcePools);
            return ResponseEntity.ok().body(outsourcePools);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
            if (!list.isEmpty()) {
                builder.and(qOutsourcePool.outBatch.in(list));
            }
            List<OutsourcePool> outsourcePools = (List<OutsourcePool>) outsourcePoolRepository.findAll(builder);
            List<AccOutsourcePoolModel> accOutsourcePoolModels = new ArrayList<>();
            for (OutsourcePool outsourcePool : outsourcePools) {
                AccOutsourcePoolModel accOutsourcePoolModel = new AccOutsourcePoolModel();
                CaseInfo caseInfo = outsourcePool.getCaseInfo();
                accOutsourcePoolModel.setCaseCode(caseInfo.getCaseNumber());
                if (Objects.nonNull(caseInfo)) {
                    if (Objects.nonNull(caseInfo.getCommissionRate())) {
                        accOutsourcePoolModel.setCommissionRate(caseInfo.getCommissionRate().toString());
                    }
                    if (Objects.nonNull(caseInfo.getContractAmount())) {
                        accOutsourcePoolModel.setContractAmount(caseInfo.getContractAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    }
                    if (Objects.nonNull(caseInfo.getPerPayAmount())) {
                        accOutsourcePoolModel.setCurrentAmount(caseInfo.getPerPayAmount().toString());//每期还款金额
                    }
                    accOutsourcePoolModel.setCurrentPayDate(ZWDateUtil.fomratterDate(caseInfo.getPerDueDate(), "yyyy-MM-dd"));//每期还款日
                    if (Objects.nonNull(caseInfo.getPersonalInfo())) {
                        accOutsourcePoolModel.setCustName(caseInfo.getPersonalInfo().getName());
                    }
                    Set set = caseInfo.getPersonalInfo().getPersonalJobs();
                    PersonalJob personalJob = null;
                    if (!set.isEmpty()) {
                        personalJob = (PersonalJob) set.iterator().next();
                    }

                    if (Objects.nonNull(personalJob)) {
                        accOutsourcePoolModel.setEmployerAddress(personalJob.getAddress());
                        accOutsourcePoolModel.setEmployerName(personalJob.getCompanyName());
                        accOutsourcePoolModel.setEmployerPhone(personalJob.getPhone());
                    }
                    if (Objects.nonNull(caseInfo.getHasPayAmount())) {
                        accOutsourcePoolModel.setHasPayAmount(caseInfo.getHasPayAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
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
                        accOutsourcePoolModel.setLastPayDate(ZWDateUtil.fomratterDate(caseInfo.getLatelyPayDate(), "yyyy-MM-dd"));//最近还款日期
                    }
                    if (Objects.nonNull(caseInfo.getOverdueAmount())) {
                        accOutsourcePoolModel.setOverDueAmount(caseInfo.getOverdueAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
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

            String[] titleList = {"案件编号", "客户姓名", "身份证号", "手机号码", "产品系列", "产品名称", "合同金额（元）", "逾期总金额（元）", "逾期本金（元）", "逾期利息（元）", "逾期罚息（元）", "逾期滞纳金（元）", "还款期数", "逾期期数", "逾期天数", "已还款金额（元）", "已还款期数", "最近还款日期", "最近还款金额（元）", "家庭住址", "工作单位名称", "工作单位地址", "工作单位电话", "佣金比例（%）", "每期还款日", "每期还款金额（元）"};
            String[] proNames = {"caseCode", "custName", "idCardNumber", "phoneNumber", "productSeries", "productName", "contractAmount", "overDueAmount", "overDueCapital", "overDueInterest", "overDueDisincentive", "overDueFine", "payPeriods", "overDuePeriods", "overDueDays", "hasPayAmount", "hasPayPeriods", "lastPayDate", "lastPayAmount", "homeAddress", "employerName", "employerAddress", "employerPhone", "commissionRate", "currentPayDate", "currentAmount"};
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
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "OutsourcePoolController")).body(body);
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
    public ResponseEntity<String> loadTemplate(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                               @RequestParam(required = true)  @ApiParam(value = "下载模板的类型") Integer type,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "userBackcashPlan", "请选择公司")).body(null);
                }
            } else {
                companyCode = user.getCompanyCode();
            }
            QSysParam qSysParam = QSysParam.sysParam;
            SysParam sysParam = null;
            if(type==0){
                sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(companyCode)
                        .and(qSysParam.code.eq(Constants.SMS_OUTCASE_ACCOUNT_URL_CODE))
                        .and(qSysParam.type.eq(Constants.SMS_OUTCASE_ACCOUNT_URL_TYPE)));
            }else{
                sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(companyCode)
                        .and(qSysParam.code.eq(Constants.SMS_OUTCASE_FOLLOWUP_URL_CODE))
                        .and(qSysParam.type.eq(Constants.SMS_OUTCASE_FOLLOWUP_URL_TYPE)));
            }

            if (Objects.isNull(sysParam)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("该模板不存在", "", "该模板不存在")).body(null);
            }
            return ResponseEntity.ok().body(sysParam.getValue());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("下载失败", "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/importFinancData")
    @ResponseBody
    @ApiOperation(value = "账目导入/委外跟进记录导入", notes = "账目导入/委外跟进记录导入")
    public ResponseEntity<List> importExcelData(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                @RequestParam(required = false) @ApiParam(value = "文件ID") String fileId,
                                                @RequestParam(required = false) @ApiParam(value = "备注") String fienRemark,
                                                @RequestParam(required = true) @ApiParam(value = "导入类型") Integer type) {
        try {
            int[] startRow = {0};
            int[] startCol = {0};

            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            AccFinanceEntry accFinanceEntry = new AccFinanceEntry();
            CaseFollowupRecord outsourceFollowRecord = new CaseFollowupRecord();
            if(type==0){
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
            } else {
                outsourceFollowRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
                outsourceFollowRecord.setOperatorName(user.getRealName());
                outsourceFollowRecord.setOperator(user.getUserName());
                if(Objects.nonNull(user.getCompanyCode())){
                    outsourceFollowRecord.setCompanyCode(user.getCompanyCode());
                }
            }

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

            List<CellError> errorList = null;
            if(type==0){
                Class<?>[] dataClass = {AccFinanceDataExcel.class};
                //解析Excel并保存到临时表中
                errorList  = accFinanceEntryService.importAccFinanceData(uploadFile.getLocalUrl(), startRow, startCol, dataClass, accFinanceEntry,outsourceFollowRecord,type);
            }else{
                Class<?>[] dataClass = {OutsourceFollowUpRecordModel.class};
                //解析Excel并保存到临时表中
                errorList  = accFinanceEntryService.importAccFinanceData(uploadFile.getLocalUrl(), startRow, startCol, dataClass, accFinanceEntry,outsourceFollowRecord,type);
            }

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
            List<OutsourcePool> outsourcePools = new ArrayList<>();  //在委外池中没有匹配的财务数据
            List<AccFinanceEntry> accFinanceEntrieAll = accFinanceEntryRepository.findAll(fienCasenums.getIdList());
            for (AccFinanceEntry financeEntryCase : accFinanceEntrieAll) {
                String caseNum = financeEntryCase.getFienCasenum();
                List<CaseInfo> caseInfos = caseInfoRepository.findByCaseNumber(caseNum);
                if (Objects.nonNull(caseInfos) && !caseInfos.isEmpty()) {
                    //对委外客户池已还款金额做累加
                    for (CaseInfo caseInfo : caseInfos) {
                        //对委外客户池中回款金额累加外部已还款金额
                        if(Objects.nonNull(caseInfo.getId())){
                            OutsourcePool outsource = outsourcePoolRepository.findOne(caseInfo.getId());
                            if(Objects.nonNull(outsource)){
                                outsource.setOutBackAmt(outsource.getOutBackAmt().add(financeEntryCase.getFienPayback()));
                                outsourcePools.add(outsource);
                            }
                        }
                    }
                } else {
                    unableMatchList.add(financeEntryCase);   //未有匹配委外案件
                }
                //临时表中的数据状态为已确认。
                financeEntryCase.setFienStatus(Status.Disable.getValue());
                accFinanceEntryList.add(financeEntryCase);
            }
            //同步更新临时表中的数据状态为已确认
            accFinanceEntryRepository.save(accFinanceEntryList);
            //更新委外的案件池里的已还款金额
            outsourcePoolRepository.save(outsourcePools);;
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

    @GetMapping("/findConfirmFinanceData")
    @ResponseBody
    @ApiOperation(value = "查询已确认的数据", notes = "查询已确认的数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<AccFinanceEntry>> findConfirmFinanceData(@ApiIgnore Pageable pageable,
                                                                        @RequestParam(required = false) @ApiParam(value = "委外方") String outsName,
                                                                        @RequestParam(required = false) @ApiParam(value = "批次号") String outbatch,
                                                                        @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                        @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            AccFinanceEntry accFinanceEntry = new AccFinanceEntry();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.nonNull(companyCode)) {
                    accFinanceEntry.setCompanyCode(companyCode);
                }
            } else {
                accFinanceEntry.setCompanyCode(user.getCompanyCode()); //限制公司code码
            }
            accFinanceEntry.setFienStatus(Status.Disable.getValue());
            accFinanceEntry.setFienCount(null);
            accFinanceEntry.setFienPayback(null);
            if (Objects.nonNull(outsName)) {
                accFinanceEntry.setFienFgname(outsName);
            }
            if (Objects.nonNull(outbatch)) {
                accFinanceEntry.setFienBatchnum(outbatch);
            }
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
            if (fienCasenums.getIdList().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("没有可删除的数据", "", "没有可删除的数据")).body(null);
            }
            for (String id : fienCasenums.getIdList()) {
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
            List<AccFinanceEntry> accOutsourcePoolList;
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            try {
                QAccFinanceEntry qAccFinanceEntry = QAccFinanceEntry.accFinanceEntry;
                BooleanBuilder builder = new BooleanBuilder();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (Objects.isNull(companyCode)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME1, "OutsourcePool", "请选择公司")).body(null);
                    }
                    builder.and(qAccFinanceEntry.companyCode.eq(companyCode));
                } else {
                    builder.and(qAccFinanceEntry.companyCode.eq(user.getCompanyCode())); //限制公司code码
                }
                if (Objects.nonNull(oupoOutbatch)) {
                    builder.and(qAccFinanceEntry.fienBatchnum.gt(oupoOutbatch));
                }
                if (Objects.nonNull(outsName)) {
                    builder.and(qAccFinanceEntry.fienFgname.like("%" + outsName + "%"));
                }
                accOutsourcePoolList = (List<AccFinanceEntry>) accFinanceEntryRepository.findAll(builder);
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
                AccFinanceEntry aop = accOutsourcePoolList.get(i);
                AccOutsideFinExportModel expm = new AccOutsideFinExportModel();
                expm.setOupoOutbatch(checkValueIsNull(aop.getFienBatchnum())); // 委外批次号
                expm.setOupoCasenum(checkValueIsNull(aop.getFienCasenum())); // 案件编号
                expm.setCustName(checkValueIsNull(aop.getFienCustname()));  // 客户名称
                expm.setOupoIdcard(checkValueIsNull(aop.getFienIdcard()));  // 身份证号
//                expm.setOupoStatus(checkOupoStatus(aop.getOutStatus())); // 委外状态
                expm.setOupoAmt(checkValueIsNull(aop.getFienCount()));  // 案件金额
                expm.setOupoPaynum(checkValueIsNull(aop.getFienPayback())); // 已还款金额
                expm.setOutsName(aop.getFienFgname());  // 委外方名称
                accOutsideList.add(expm);
            }

            // 将存放的数据写入Excel
            String[] titleList = {"案件编号", "客户姓名", "客户身份证号", "委外方", "案件金额", "已还款金额"};
            String[] proNames = {"oupoCasenum", "custName", "oupoIdcard", "outsName", "oupoAmt", "oupoPaynum"};
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
    public ResponseEntity<Page<OutsourceRecord>> getAccOutsourceOrder(@RequestParam(required = false) @ApiParam(value = "委外方") String outsName,
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

    /**
     * @Description 按批次号查看委外案件详情
     * <p>
     * Created by huyanmin at 2017/09/26
     */
    @GetMapping("/getOutSourceCaseByBatchnum")
    @ApiOperation(value = "按批次号查看委外案件详情", notes = "按批次号查看委外案件详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutsourcePool>> getOutSourceCaseByBatchnum(@RequestParam(required = true) @ApiParam(value = "批次号") String batchNumber,
                                                                          @RequestParam(required = true) @ApiParam(value = "委外方名称") String outsName,
                                                                          @RequestParam(required = false) @ApiParam(value = "公司Code码") String companyCode,
                                                                          @QuerydslPredicate(root = OutsourcePool.class) Predicate predicate,
                                                                          @ApiIgnore Pageable pageable,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("Rest request get outsource case by batch number");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePoolController", "user do not log in", e.getMessage())).body(null);
        }
        try {
            QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if(Objects.nonNull(batchNumber)){
                builder.and(qOutsourcePool.outBatch.eq(batchNumber));
            }
            if(Objects.nonNull(outsName)){
                Outsource outsource = outsourceRepository.findOne(QOutsource.outsource.outsName.eq(outsName));
                builder.and(qOutsourcePool.outsource.id.eq(outsource.getId()));
            }
            builder.and(qOutsourcePool.outStatus.eq(OutsourcePool.OutStatus.OUTSIDING.getCode()));
            builder.and(qOutsourcePool.caseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()));
            Page<OutsourcePool> page = outsourcePoolRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePoolController", "getAllClosedOutSourceCase", "系统异常!")).body(null);
        }

    }

    /**
     * @Description 查看委外案件跟进记录
     * <p>
     * Created by huyanmin at 2017/09/27
     */
    @GetMapping("/getOutSourceCaseFollowRecord")
    @ApiOperation(value = "查看委外案件跟进记录", notes = "查看委外案件跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseFollowupRecord>> getOutSourceCaseFollowRecord(@RequestParam(required = true) @ApiParam(value = "案件编号") String caseNumber,
                                                                                    @QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                                    @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                                    @ApiIgnore Pageable pageable,
                                                                                    @RequestHeader(value = "X-UserToken") String token) {
        log.debug("Rest request get outsource case by batch number");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePoolController", "user do not log in", e.getMessage())).body(null);
        }
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if(Objects.nonNull(caseNumber)){
                builder.and(QCaseFollowupRecord.caseFollowupRecord.caseNumber.eq(caseNumber));
            }

            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseFollowupType.eq(CaseFollowupRecord.CaseFollowupType.OUTER.getValue()));
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OutsourcePoolController", "getAllClosedOutSourceCase", "系统异常!")).body(null);
        }
    }

    @PostMapping("/returnOutsourceCase")
    @ApiOperation(value = "回收委外案件", notes = "回收委外案件")
    public ResponseEntity<List<CaseInfoReturn>> returnOutsourceCase(@RequestBody OutCaseIdList outCaseIdList,
                                                                    @RequestHeader(value = "X-UserToken") String token) {
        try {
            List<String> outCaseIds = outCaseIdList.getOutCaseIds();
            if (Objects.isNull(outCaseIds) || outCaseIds.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要回收的案件")).body(null);
            }
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            List<OutsourcePool> all = outsourcePoolRepository.findAll(outCaseIds);
            Iterator<OutsourcePool> iterator = all.iterator();
            List<CaseInfoReturn> caseInfoReturnList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            while (iterator.hasNext()) {
                OutsourcePool outsourcePool = iterator.next();
                CaseInfo caseInfo = outsourcePool.getCaseInfo();
                caseInfo.setOperatorTime(new Date());
                caseInfo.setOperator(user);
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());
                outsourcePool.setCaseInfo(caseInfo);
                outsourcePoolList.add(outsourcePool);

                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setSource(CaseInfoReturn.Source.OUTSOURCE.getValue());
                caseInfoReturn.setOutsourcePool(outsourcePool);
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturn.setOperator(user.getId());
                caseInfoReturn.setReason(outCaseIdList.getReturnReason());
                caseInfoReturnList.add(caseInfoReturn);
            }
            outsourcePoolRepository.save(outsourcePoolList);
            caseInfoReturnRepository.save(caseInfoReturnList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("回收成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("案件回收失败", ENTITY_NAME1, e.getMessage())).body(null);
        }
    }

}
