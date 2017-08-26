package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.MapModel;
import cn.fintecher.pangolin.business.model.PersonalInfoExportModel;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseTurnRecordRepository;
import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.service.AccMapService;
import cn.fintecher.pangolin.business.service.PersonalInfoExportService;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static cn.fintecher.pangolin.entity.QCaseInfo.caseInfo;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/personalController")
@Api(value = "PersonalController", description = "客户信息操作")
public class PersonalController extends BaseController {

    private static final String ENTITY_NAME = "personal";
    private static final String ENTITY_CASE_TURN_RECORD = "CaseTurnRecord";
    private static final String ENTITY_CASE_FOLLOWUP_RECORD = "CaseFollowupRecord";
    private final Logger log = LoggerFactory.getLogger(PersonalController.class);

    @Inject
    private PersonalRepository personalRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private RestTemplate restTemplate;
    @Inject
    private PersonalInfoExportService personalInfoExportService;
    @Inject
    private CaseTurnRecordRepository caseTurnRecordRepository;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    EntityManager em;
    @Inject
    AccMapService accMapService;


    @PostMapping("/personalInfoExport")
    @ApiOperation(value = "客户信息导出", notes = "客户信息导出")
    public ResponseEntity personalInfoExport(@RequestBody @ApiParam("配置项") PersonalInfoExportModel model) {
        Integer exportType = model.getExportType();  //导出维度  0-催收员，1-产品类型，2-批次号，3-案件状态
        // 数据过滤
        Map<String, List<Object>> dataFilter = model.getDataFilter();
        Map<String, List<String>> dataInfo = model.getDataInfo(); //数据项


        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        try {
            QCaseInfo qCaseInfo = caseInfo;
            Map<String, String> headMap; //存储头信息
            List<Map<String, Object>> dataList; //存储数据信息
            List<CaseInfo> caseInfos = new ArrayList<>(); //数据
            List<List<String>> list = new ArrayList<>(); //选项
            Integer maxNum = null; //最大联系人数
            // 催收员
            if (Objects.equals(exportType, 0)) {
                // 查找出所有属于该催收员的数据
                String orgCode = null;// 组织机构Code
                String collectorName = null; //催收员名称
                List<Object> orgObj = dataFilter.get("org");
                if (Objects.isNull(orgObj) || orgObj.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "数据筛选机构为空!")).body(null);
                } else {
                    orgCode = (String) orgObj.get(0);
                }
                List<Object> collectorObj = dataFilter.get("collector");
                if (Objects.isNull(collectorObj) || collectorObj.isEmpty()) {
                    collectorName = null;
                } else {
                    collectorName = (String) collectorObj.get(0);
                }
                // 部门下的催收员
                Department one = departmentRepository.findOne(orgCode);
                BooleanExpression exp = qCaseInfo.department.code.startsWith(one.getCode());
                exp.and(qCaseInfo.currentCollector.realName.eq(collectorName));
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                caseInfos = IterableUtils.toList(all);
                if (caseInfos.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "要导出的数据为空!")).body(null);
                }
                List<String> collect = dataInfo.get("collect"); // 催收员为维度数据选选项
                if (Objects.isNull(collect) || collect.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "选项为空!")).body(null);
                }
                list.add(collect);
            }
            // 产品类型
            if (Objects.equals(exportType, 1)) {
                String prodName = null; //产品名称
                List<Object> prodObj = dataFilter.get("prodName");
                if (Objects.isNull(prodObj) || prodObj.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "数据筛选产品名称为空!")).body(null);
                } else {
                    prodName = (String) prodObj.get(0);
                }
                // 查找出某产品类型的所有案件
                BooleanExpression exp = qCaseInfo.product.productSeries.seriesName.eq(prodName);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                caseInfos = IterableUtils.toList(all);
                if (caseInfos.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "要导出的数据为空!")).body(null);
                }
                // 以产品类型为维度选项
                List<String> baseInfo = dataInfo.get("baseInfo"); // 基本信息
                List<String> workInfo = dataInfo.get("workInfo"); // 工作信息
                List<String> contactInfo = dataInfo.get("contactInfo"); // 联系人信息
                List<String> bankInfo = dataInfo.get("bankInfo"); // 开户信息
                if ((Objects.isNull(baseInfo) || baseInfo.isEmpty())
                        && (Objects.isNull(workInfo) || workInfo.isEmpty())
                        && (Objects.isNull(contactInfo) || contactInfo.isEmpty())
                        && (Objects.isNull(bankInfo) || bankInfo.isEmpty())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "选项为空!")).body(null);
                }
                list.add(baseInfo);
                list.add(workInfo);
                list.add(contactInfo);
                list.add(bankInfo);
                maxNum = personalInfoExportService.getMaxNum(caseInfos);
            }
            // 批次号
            if (Objects.equals(exportType, 2)) {
                String batchNumber = null; //批次号
                List<Object> batchNumObj = dataFilter.get("batchNumber");
                if (Objects.isNull(batchNumObj) || batchNumObj.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "数据筛选产品名称为空!")).body(null);
                } else {
                    batchNumber = (String) batchNumObj.get(0);
                }
                BooleanExpression exp = qCaseInfo.batchNumber.eq(batchNumber);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                caseInfos = IterableUtils.toList(all);
                if (caseInfos.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "要导出的数据为空!")).body(null);
                }
                List<String> batch = dataInfo.get("batch"); // 批次号为维度数据选选项
                if (Objects.isNull(batch) || batch.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "选项为空!")).body(null);
                }
                list.add(batch);
            }

            // 案件状态
            if (Objects.equals(exportType, 3)) {
                List<Object> stList = (List) dataFilter.get("caseInfoStatus");
                if (Objects.isNull(stList) || stList.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "数据筛选产品名称为空!")).body(null);
                }
                List<Integer> sl = new ArrayList<>();
                for (Object o : stList) {
                    sl.add(Integer.valueOf(o.toString()));
                }
                BooleanExpression exp = qCaseInfo.collectionStatus.in(sl);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                caseInfos = IterableUtils.toList(all);
                if (caseInfos.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "要导出的数据为空!")).body(null);
                }
                List<String> caseStatus = dataInfo.get("caseStatus"); // 案件状态为维度数据选选项
                if (Objects.isNull(caseStatus) || caseStatus.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "选项为空!")).body(null);
                }
                list.add(caseStatus);
            }
            headMap = personalInfoExportService.createHeadMap(exportType, list, maxNum);
            dataList = personalInfoExportService.createDataList(caseInfos);
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("客户信息");
            ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "客户信息表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "上传服务器失败!")).body(null);
            } else {
                return ResponseEntity.ok().body(url);
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("PersonalController", "personalInfoExport", "导出失败!")).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }

    @PostMapping("/personal")
    public ResponseEntity<Personal> createPersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to save personal : {}", personal);
        if (personal.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.created(new URI("/api/personal/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/personal")
    public ResponseEntity<Personal> updatePersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to update Personal : {}", personal);
        if (personal.getId() == null) {
            return createPersonal(personal);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, personal.getId().toString()))
                .body(result);
    }

    @GetMapping("/personal")
    public List<Personal> getAllPersonal() {
        log.debug("REST request to get all Personal");
        List<Personal> personalList = personalRepository.findAll();
        return personalList;
    }

    @GetMapping("/queryPersonal")
    public ResponseEntity<Page<Personal>> queryPersonal(@QuerydslPredicate(root = Personal.class) Predicate predicate, @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get all Personal");

        Page<Personal> page = personalRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryPersonal");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<Personal> getPersonal(@PathVariable String id) {
        log.debug("REST request to get personal : {}", id);
        Personal personal = personalRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personal));
    }

    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Void> deletePersonal(@PathVariable String id) {
        log.debug("REST request to delete personal : {}", id);
        personalRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * @Description 客户查询
     */
    @GetMapping("/getPersonalCaseInfo")
    @ApiOperation(value = "客户查询", notes = "客户查询（分页、条件）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<Page<CaseInfo>> getPersonalCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable,
                                                              @RequestHeader(value = "X-UserToken") String token,
                                                              @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) throws URISyntaxException {
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.equals(tokenUser.getUserName(), "administrator")) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseRepair", "", "请选择公司")).body(null);
                }
                builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            } else {
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode()));
            }
            Page<CaseInfo> page = caseInfoRepository.findAll(predicate, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/PersonalController/getPersonalCaseInfo");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 查询案件流转记录
     */
    @GetMapping("/getCaseTurnRecord")
    @ApiOperation(value = "查询案件流转记录", notes = "查询案件流转记录")
    public ResponseEntity<List<CaseTurnRecord>> getCaseTurnRecord(@RequestParam("caseNumber") @ApiParam(value = "案件编号", required = true) String caseNumber,
                                                                  @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get case turn record by {caseNumber}", caseNumber);
        try {
            User tokenUser = getUserByToken(token);
            OrderSpecifier<Integer> sortOrder = QCaseTurnRecord.caseTurnRecord.id.asc();
            QCaseTurnRecord qCaseTurnRecord = QCaseTurnRecord.caseTurnRecord;
            Iterable<CaseTurnRecord> caseTurnRecords = caseTurnRecordRepository.findAll(qCaseTurnRecord.caseNumber.eq(caseNumber)
                    .and(qCaseTurnRecord.companyCode.eq(tokenUser.getCompanyCode())), sortOrder);
            List<CaseTurnRecord> caseTurnRecordList = IterableUtils.toList(caseTurnRecords);
            //过滤掉接收部门为为空的数据
            caseTurnRecordList.forEach(e -> {
                if (Objects.isNull(e.getReceiveDeptName())) {
                    e.setReceiveDeptName("未知");
                }
            });
            if (caseTurnRecordList.isEmpty()) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("该案件跟进记录为空", ENTITY_CASE_TURN_RECORD)).body(new ArrayList<>());
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_CASE_TURN_RECORD)).body(caseTurnRecordList);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_TURN_RECORD, "caseTurnRecord", "查询失败")).body(null);
        }
    }

    @GetMapping("/getMapInfo")
    @ApiOperation(value = "查询客户地图", notes = "查询客户地图")
    public ResponseEntity<MapModel> getMapInfo(@RequestParam @ApiParam(value = "客户地址", required = true) String address) {
        try {
            MapModel model = accMapService.getAddLngLat(address);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", null)).body(model);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("RepairCaseDistributeController", "error", e.getMessage())).body(null);
        }

    }
}
