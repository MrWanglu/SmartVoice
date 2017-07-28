package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.PersonalInfoExportModel;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.PersonalContact;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.*;
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
    private final Logger log = LoggerFactory.getLogger(PersonalController.class);

    @Inject
    private PersonalRepository personalRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private RestTemplate restTemplate;

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
            // 催收员
            if (Objects.equals(exportType, 0)) {
                String[] collectData = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "催收员"};
                String[] collectPro = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "collector"};
                List<String> collect = dataInfo.get("collect"); // 催收员为维度数据选选项
                // 查找出所有属于该催收员的数据
                String orgCode = (String) dataFilter.get("org").get(0);// 组织机构Code
                String collectorName = (String) dataFilter.get("collector").get(0); //催收员名称
                // 部门下的催收员
                BooleanExpression exp = qCaseInfo.department.code.startsWith(orgCode);
                exp.and(qCaseInfo.currentCollector.realName.eq(collectorName));
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                Iterator<CaseInfo> iterator = all.iterator();
                if (!iterator.hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("要导出的数据为空!", "要导出的数据为空!")).body(null);
                }
                Map<String, String> headMap = new HashMap<>(); //存储头信息
                // 遍历collect
                for (int i = 0; i < collect.size(); i++) {
                    for (int k = 0; k < collectData.length; k++) {
                        if (Objects.equals(collect.get(i), collectData[k])) {
                            headMap.put(collectPro[k], collect.get(i));
                        }
                    }
                }
                List<Map<String, Object>> dataList = new ArrayList<>(); //存储数据信息
                // 遍历数据
                while (iterator.hasNext()) {
                    CaseInfo caseInfo = all.iterator().next();
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptName", caseInfo.getDepartment().getName());
                    map.put("custName", caseInfo.getPersonalInfo().getName());
                    map.put("idCard", caseInfo.getPersonalInfo().getIdCard());
                    map.put("phone", caseInfo.getPersonalInfo().getMobileNo());
                    map.put("city", caseInfo.getPersonalInfo().getIdCardAddress());
                    map.put("periods", caseInfo.getPeriods());
                    map.put("overDays", caseInfo.getOverdueDays());
                    map.put("overAmt", caseInfo.getOverdueAmount());
                    map.put("loanDate", caseInfo.getLoanDate());
                    map.put("payStatus", caseInfo.getPayStatus());
                    map.put("collector", caseInfo.getCurrentCollector().getRealName());
                    dataList.add(map);
                }
                workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("客户信息");
                ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);

            }
            // 产品类型
            if (Objects.equals(exportType, 1)) {
                String[] baseInfoData = {"客户姓名", "身份证号", "归属城市", "手机号", "身份证户籍地址", "家庭地址", "家庭电话"};
                String[] baseInfoPro = {"custName", "idCard", "city", "phone", "idCardAddress", "homeAddress", "homePhone"};

                String[] workInfoData = {"工作单位名称", "工作单位地址", "工作单位电话"};
                String[] workInfoPro = {"workName", "workAddress", "workPhone"};

                String[] contactInfoData = {"关系", "姓名", "手机号码", "住宅电话", "现居住地址", "工作单位", "单位电话"};
                String[] contactInfoPro = {"relation", "contactName", "contactPhone", "contactHomePhone", "contactAddress", "contactWorkCompany", "contactWorkPhone"};

                String[] bankInfoData = {"还款卡银行", "还款卡号"};
                String[] bankInfoPro = {"bankName", "bankCard"};

                // 以产品类型为维度选项
                List<String> baseInfo = dataInfo.get("baseInfo"); // 基本信息
                List<String> workInfo = dataInfo.get("workInfo"); // 工作信息
                List<String> contactInfo = dataInfo.get("contactInfo"); // 联系人信息
                List<String> bankInfo = dataInfo.get("bankInfo"); // 开户信息

                // 查找出某产品类型的所有案件
                String prodName = (String)dataFilter.get("prodName").get(0);//产品名称
                BooleanExpression exp = qCaseInfo.product.productSeries.seriesName.eq(prodName);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                Iterator<CaseInfo> iterator = all.iterator();
                if (!iterator.hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("要导出的数据为空!", "要导出的数据为空!")).body(null);
                }
                Map<String, String> headMap = new HashMap<>(); //存储头信息
                // 遍历选项
                for (int i = 0; i < baseInfo.size(); i++) {
                    for (int k = 0; k < baseInfoData.length; k++) {
                        if (Objects.equals(baseInfo.get(i), baseInfoData[k])) {
                            headMap.put(baseInfoPro[k], baseInfo.get(i));
                        }
                    }
                }
                for (int i = 0; i < workInfo.size(); i++) {
                    for (int k = 0; k < workInfoData.length; k++) {
                        if (Objects.equals(baseInfo.get(i), workInfoData[k])) {
                            headMap.put(workInfoPro[k], workInfo.get(i));
                        }
                    }
                }

                int num = 0;
                for (int i = 0; i < contactInfo.size(); i++) {
                    for (int k = 0; k < contactInfoData.length; k++) {
                        if (Objects.equals(contactInfo.get(i), contactInfoData[k])) {
                            // 遍历所有的案件 获取联系人最多的案件的联系人的数量
                            while (iterator.hasNext()) {
                                CaseInfo caseInfo = iterator.next();
                                Set<PersonalContact> personalContacts = caseInfo.getPersonalInfo().getPersonalContacts();
                                if (Objects.nonNull(personalContacts) && personalContacts.size() > num) {
                                    num = personalContacts.size();
                                }
                            }
                            if (num != 0) {
                                for (int m = 1; m <= num; m++) {
                                    headMap.put(contactInfoPro[k] + m, contactInfo.get(i) + m);
                                }
                            }

                        }
                    }
                }
                for (int i = 0; i < bankInfo.size(); i++) {
                    for (int k = 0; k < bankInfoData.length; k++) {
                        if (Objects.equals(bankInfo.get(i), bankInfoData[k])) {
                            headMap.put(bankInfoPro[k], bankInfo.get(i));
                        }
                    }
                }
                List<Map<String, Object>> dataList = new ArrayList<>(); //存储数据信息
                // 遍历数据
                while (iterator.hasNext()) {
                    CaseInfo caseInfo = all.iterator().next();
                    Map<String, Object> map = new HashMap<>();
                    map.put("custName", caseInfo.getPersonalInfo().getName());
                    map.put("idCard", caseInfo.getPersonalInfo().getIdCard());
                    map.put("city", caseInfo.getPersonalInfo().getIdCardAddress());
                    map.put("phone", caseInfo.getPersonalInfo().getMobileNo());
                    map.put("idCardAddress", caseInfo.getPersonalInfo().getIdCardAddress());
                    map.put("homeAddress", caseInfo.getPersonalInfo().getLocalHomeAddress());
                    map.put("homePhone", caseInfo.getPersonalInfo().getLocalPhoneNo());
                    map.put("workName", caseInfo.getPersonalInfo().getPersonalJobs().iterator().next().getCompanyName()); //单位名称
                    map.put("workAddress", caseInfo.getPersonalInfo().getPersonalJobs().iterator().next().getAddress()); //单位地址
                    map.put("workPhone", caseInfo.getPersonalInfo().getPersonalJobs().iterator().next().getPhone()); //单位电话
                    Set<PersonalContact> personalContacts = caseInfo.getPersonalInfo().getPersonalContacts();
                    PersonalContact[] p = (PersonalContact[]) personalContacts.toArray();
                    if (num != 0) {
                        for (int m = 1; m <= num; m++) {
                            if (personalContacts.isEmpty() || m > personalContacts.size()) {
                                map.put("relation" + m, "");
                                map.put("contactName" + m, "");
                                map.put("contactPhone" + m, "");
                                map.put("contactHomePhone" + m, "");
                                map.put("contactAddress" + m, "");
                                map.put("contactWorkAddress" + m, "");
                                map.put("contactWorkPhone" + m, "");
                            } else {
                                map.put("relation" + m, p[m].getRelation());
                                map.put("contactName" + m, p[m].getName());
                                map.put("contactPhone" + m, p[m].getMobile());
                                map.put("contactHomePhone" + m, p[m].getPhone());
                                map.put("contactAddress" + m, p[m].getAddress());
                                map.put("contactWorkCompany" + m, p[m].getEmployer());
                                map.put("contactWorkPhone" + m, p[m].getWorkPhone());
                            }
                        }
                    }
                    map.put("bankName", caseInfo.getPersonalInfo().getPersonalBankInfos().iterator().next().getDepositBank());
                    map.put("bankCard", caseInfo.getPersonalInfo().getPersonalBankInfos().iterator().next().getCardNumber());
                    dataList.add(map);
                }
                workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("客户信息");
                ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            }
            // 批次号
            if (Objects.equals(exportType, 2)) {
                String[] batchNumData = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "批次号"};
                String[] batchNumPro = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "batchNum"};
                List<String> batch = dataInfo.get("batch"); // 催收员为维度数据选选项

                String batchNumber = (String)dataFilter.get("batchNumber").get(0); //批次号
                BooleanExpression exp = qCaseInfo.batchNumber.eq(batchNumber);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                Iterator<CaseInfo> iterator = all.iterator();
                if (!iterator.hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("要导出的数据为空!", "要导出的数据为空!")).body(null);
                }
                Map<String, String> headMap = new HashMap<>(); //存储头信息
                // 遍历batch
                for (int i = 0; i < batch.size(); i++) {
                    for (int k = 0; k < batchNumData.length; k++) {
                        if (Objects.equals(batch.get(i), batchNumData[k])) {
                            headMap.put(batchNumPro[k], batch.get(i));
                        }
                    }
                }
                List<Map<String, Object>> dataList = new ArrayList<>(); //存储数据信息
                // 遍历数据
                while (iterator.hasNext()) {
                    CaseInfo caseInfo = all.iterator().next();
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptName", caseInfo.getDepartment().getName());
                    map.put("custName", caseInfo.getPersonalInfo().getName());
                    map.put("idCard", caseInfo.getPersonalInfo().getIdCard());
                    map.put("phone", caseInfo.getPersonalInfo().getMobileNo());
                    map.put("city", caseInfo.getPersonalInfo().getIdCardAddress());
                    map.put("periods", caseInfo.getPeriods());
                    map.put("overDays", caseInfo.getOverdueDays());
                    map.put("overAmt", caseInfo.getOverdueAmount());
                    map.put("loanDate", caseInfo.getLoanDate());
                    map.put("payStatus", caseInfo.getPayStatus());
                    map.put("batchNum", caseInfo.getBatchNumber());
                    dataList.add(map);
                }
                workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("客户信息");
                ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            }

            // 案件状态
            if (Objects.equals(exportType, 3)) {
                String[] caseStatusData = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "案件状态"};
                String[] caseStatusPro = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "caseStatus"};
                List<String> caseStatus = dataInfo.get("caseStatus"); // 案件状态为维度数据选选项

                List<Integer> stList = (List)dataFilter.get("caseInfoStatus");//状态
                BooleanExpression exp = qCaseInfo.collectionStatus.in(stList);
                Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
                Iterator<CaseInfo> iterator = all.iterator();
                if (!iterator.hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("要导出的数据为空!", "要导出的数据为空!")).body(null);
                }
                Map<String, String> headMap = new HashMap<>(); //存储头信息
                // 遍历batch
                for (int i = 0; i < caseStatus.size(); i++) {
                    for (int k = 0; k < caseStatusData.length; k++) {
                        if (Objects.equals(caseStatus.get(i), caseStatusData[k])) {
                            headMap.put(caseStatusPro[k], caseStatus.get(i));
                        }
                    }
                }
                List<Map<String, Object>> dataList = new ArrayList<>(); //存储数据信息
                // 遍历数据
                while (iterator.hasNext()) {
                    CaseInfo caseInfo = all.iterator().next();
                    Map<String, Object> map = new HashMap<>();
                    map.put("deptName", caseInfo.getDepartment().getName());
                    map.put("custName", caseInfo.getPersonalInfo().getName());
                    map.put("idCard", caseInfo.getPersonalInfo().getIdCard());
                    map.put("phone", caseInfo.getPersonalInfo().getMobileNo());
                    map.put("city", caseInfo.getPersonalInfo().getIdCardAddress());
                    map.put("periods", caseInfo.getPeriods());
                    map.put("overDays", caseInfo.getOverdueDays());
                    map.put("overAmt", caseInfo.getOverdueAmount());
                    map.put("loanDate", caseInfo.getLoanDate());
                    map.put("payStatus", caseInfo.getPayStatus());
                    map.put("caseStatus", caseInfo.getCollectionStatus());
                    dataList.add(map);
                }
                workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet("客户信息");
                ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            }
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = File.separator.concat(DateTime.now().toString("yyyyMMddhhmmss") + "客户信息表.xls");
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
     * @Description 费用减免审批页面多条件查询减免记录
     */
    @GetMapping("/getPersonalCaseInfo")
    @ApiOperation(value = "客户查询", notes = "客户查询（分页、条件）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<Page<CaseInfo>> getPersonalCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable) throws URISyntaxException {
        try {
        Page<CaseInfo> page = caseInfoRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/PersonalController/getPersonalCaseInfo");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }


}
