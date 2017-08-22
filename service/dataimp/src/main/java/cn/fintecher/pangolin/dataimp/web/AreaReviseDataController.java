package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.CellError;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.QDataInfoExcel;
import cn.fintecher.pangolin.dataimp.model.AreaResult;
import cn.fintecher.pangolin.dataimp.model.ListResult;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelRepository;
import cn.fintecher.pangolin.dataimp.service.AreaReviseDataService;
import cn.fintecher.pangolin.dataimp.util.ExcelUtil;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @Author:liuxiang
 * @Description: 区域解析功能
 * @Date:2017-8-8
 */
@RestController
@RequestMapping(value = "/api/areaReviseDataController")
@Api(value = "区域解析", description = "区域解析")
public class AreaReviseDataController {

    @Autowired
    AreaReviseDataService areaReviseDataService;
    @Autowired
    DataInfoExcelRepository dataInfoExcelRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MongoTemplate mongoTemplate;
    private final Logger logger = LoggerFactory.getLogger(AreaReviseDataController.class);
    private static final String ENTITY_NAME = "DataInfoExcel";


    //从前台接受get请求，发起异步分页请求，从数据库查询数据
    @GetMapping("/findAreaInfo")
    @ApiOperation(value = "按条件查询原来的信息", notes = "按条件查询原信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "interger",
                    paramType = "query", value = "页数(0..N页)"),
            @ApiImplicitParam(name = "size", dataType = "interger",
                    paramType = "query", value = "当前页数"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string",
                    paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<DataInfoExcel>> findAreaInfo(@QuerydslPredicate(root = DataInfoExcel.class) Predicate predicate,
                                       @ApiIgnore  Pageable pageable,
                                       @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
//        ResponseEntity<User> userResponseEntity = null;
//        try {
//            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
//        } catch (Exception e) {
////            logger.error(e.getMessage(),e);
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
//        }
//        if (!userResponseEntity.hasBody()) {
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", Constants.SYS_EXCEPTION_NOSESSION)).body(null);
//        }
//
//            User user = userResponseEntity.getBody();
//            BooleanBuilder builder = new BooleanBuilder(predicate);
//            builder.and(QDataInfoExcel.dataInfoExcel.operator.eq(user.getId()));
//            Page<DataInfoExcel> dataInfoExcelPage = dataInfoExcelRepository.findAll(builder, pageable);
//            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询数据成功",ENTITY_NAME)).body(dataInfoExcelPage);
        return null;

    }


    @PostMapping("/importOriginalData//{fileId}")
    @ApiOperation(value = "导入功能", notes = "导入功能")
    public ResponseEntity<UploadFile> importOriginalData(@PathVariable("fileId") String fileId,
                                                         @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {

        //获取用户
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        if (!userResponseEntity.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", Constants.SYS_EXCEPTION_NOSESSION)).body(null);
        }

        User user = userResponseEntity.getBody();

        try {
            //导入前先清空上次导入存储的数据
            Query query = new Query();
            query.addCriteria(Criteria.where("operator").is(user.getId()));
            mongoTemplate.remove(query, DataInfoExcel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "系统错误！")).body(null);
        }

        int[] startRow = {0};
        int[] startCol = {0};
        Class<?>[] dataClass = {DataInfoExcel.class};
        UploadFile uploadFile;
        UploadFile file;
        try {
            ParameterizedTypeReference<UploadFile> responseType = new ParameterizedTypeReference<UploadFile>() {
            };
            ResponseEntity<UploadFile> resp = restTemplate.getForEntity(Constants.FILEID_SERVICE_URL.concat("uploadFile/").concat(fileId), UploadFile.class);//restTemplate.exchange(Constants.FILEID_SERVICE_URL.concat("uploadFile/getUploadFile/").concat(fileId), HttpMethod.GET, null, responseType);
            uploadFile = resp.getBody();
            //uploadFile = UploadFileResource.getUploadFile(fileId);
            if (Objects.isNull(uploadFile)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "获取上传文件失败！")).body(null);
            } else {
                file = uploadFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "系统错误")).body(null);

        }

        //解析Excel
        List<CellError> cellErrorList = new ArrayList<>();
        try {
            cellErrorList = areaReviseDataService.importOriginalData(file, startRow, startCol, dataClass, null,user);
            if (!cellErrorList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", cellErrorList.get(0).getErrorMsg())).body(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "系统异常！")).body(null);
        }

        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("导入数据成功", ENTITY_NAME)).body(null);

    }

    @GetMapping("/exportReviseData")
    @ApiOperation(value = "导出解析后得数据", notes = "d导出解析后得数据")
    public ResponseEntity<Page<DataInfoExcel>> exportReviseData(@QuerydslPredicate(root = DataInfoExcel.class) Predicate predicate,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") @ApiParam("操作者的token") String token
    ) {

        //获取用户
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        if (!userResponseEntity.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", Constants.SYS_EXCEPTION_NOSESSION)).body(null);
        }
        User user = userResponseEntity.getBody();
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        try {

            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QDataInfoExcel.dataInfoExcel.operator.eq(user.getId()));
            List<DataInfoExcel> all = dataInfoExcelRepository.findAll(builder, pageable).getContent();
            if (all.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "根据查询条件得到要导出的数据为空")).body(null);
            }


            // 将存放的数据写入Excel
            String[] titleList = {"客户姓名", "身份证号", "手机号码", "产品系列", "产品名称", "合同编号", "贷款日期", "还款期数",
                    "每期还款日", "每期还款金额(元)", "合同金额(元)", "剩余本金(元)", "剩余利息(元)", "逾期总金额(元)", "逾期本金(元)",
                    "逾期利息(元)", "逾期罚息(元)", "逾期滞纳金(元)", "其他费用(元)", "逾期日期", "逾期期数", "逾期天数", "已还款金额(元)",
                    "已还款期数", "最近还款日期", "最近还款金额(元)", "客户还款卡银行", "客户还款卡号", "省份", "城市", "家庭住址",
                    "家庭固话", "身份证户籍地址", "工作单位名称", "工作单位地址", "工作单位电话", "联系人1姓名", "联系人1与客户关系",
                    "联系人1工作单位", "联系人1单位电话", "联系人1手机号码", "联系人1住宅电话", "联系人1现居地址", "联系人2姓名",
                    "联系人2与客户关系", "联系人2工作单位", "联系人2单位电话", "联系人2手机号码", "联系人2住宅电话", "联系人2现居地址",
                    "联系人3姓名", "联系人3与客户关系", "联系人3工作单位", "联系人3单位电话", "联系人3手机号码", "联系人3住宅电话",
                    "联系人3现居地址", "联系人4姓名", "联系人4与客户关系", "联系人4工作单位", "联系人4单位电话", "联系人4手机号码",
                    "联系人4住宅电话", "联系人4现居地址", "备注", "佣金比例(%)","逾期管理费","还款状态","批次号","委托方编号","委托方名称","委案日期",
                    "结案日期","创建时间","操作人员","操作人姓名","数据来源0-Excel导入","案件手数","公司码","案件编号"
            };
            String[] proNames = {"personalName", " idCard", "mobileNo", "productSeriesName", "productName", "contractNumber",
                    "loanDate", "periods", "perDueDate", "perPayAmount", "contractAmount", "leftCapital",
                    "leftInterest", "overdueAmount", " overdueCapital", "overDueInterest", "overdueFine",
                    "overdueDelayFine", "otherAmt", " overDueDate", "overDuePeriods", "overDuePeriods", "overDueDays",
                    "hasPayAmount", "hasPayPeriods", "latelyPayDate", "depositBank", "cardNumber", "province", "city",
                    "homeAddress", "homePhone", "idCardAddress", "companyName", "companyAddr", "companyPhone", "contactName1",
                    "contactRelation1", "contactWorkUnit1", "contactUnitPhone1", "contactPhone1", "contactHomePhone1",
                    "contactCurrAddress1", "contactName2", "contactRelation2", "contactWorkUnit2",
                    "contactUnitPhone2", "contactPhone2", "contactHomePhone2", "contactCurrAddress2", "contactName3", "contactRelation3",
                    "contactWorkUnit3", "contactUnitPhone3", "contactPhone3", "contactHomePhone3", "contactCurrAddress3",
                    "contactName4", "contactRelation4", "contactWorkUnit4", "contactUnitPhone4", "contactPhone4", "contactHomePhone4",
                    "contactCurrAddress4", "memo", "commissionRate","overdueManageFee","paymentStatus","batchNumber","prinCode","prinName",
                    "delegationDate"," closeDate"," operatorTime","operator"," operatorName"," dataSources","caseHandNum","companyCode"," caseNumber"
            };
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            ExcelUtil.createExcel(workbook, sheet, all, titleList, proNames, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(ZWDateUtil.fomratterDate(ZWDateUtil.getNowDateTime(), "yyyyMMddhhmmss") + "财务数据对账.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<String, Object>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", Constants.ERROR_MESSAGE)).body(null);
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(url.getBody(), ENTITY_NAME)).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", "导出文件服务器失败")).body(null);

        } finally {
            //关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //删除文件
            if (file != null) {
                file.delete();
            }

            //导出后将数据库中的数据删除
            Query query = new Query();
            query.addCriteria(Criteria.where("operator").is(user.getId()));
            mongoTemplate.remove(query, DataInfoExcel.class);
        }

    }


    @GetMapping("/reviseAllAreaInfo/{style}")
    @ApiOperation(value = "一建解析区域信息", notes = "一建解析区域信息")
    @Transactional
    public ResponseEntity reviseAllAreaInfo(@PathVariable(value = "style", required = true) @ApiParam("解析维度") Integer style,
                                            @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {


        //获取用户
        ResponseEntity<User> userResponseEntity = null;
        try {
            restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        if (!userResponseEntity.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", Constants.SYS_EXCEPTION_NOSESSION)).body(null);

        }
        User user = userResponseEntity.getBody();
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QDataInfoExcel.dataInfoExcel.operator.eq(user.getId()));
            List<DataInfoExcel> all = IterableUtils.toList(dataInfoExcelRepository.findAll(builder));
            if (all.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "数据不存在！")).body(null);
            }

            //创建一个线程，执行解析任务
            Thread t = new Thread(() -> {
                long st = System.currentTimeMillis();
                List<DataInfoExcel> clist = new ArrayList<DataInfoExcel>();
                ListResult listResult = new ListResult();
                listResult.setUser(user.getId());
                try {
                    clist = areaReviseDataService.reviseStrategic(all, style, user.getId());
                    listResult.setResult(clist);
                    listResult.setStatus(ListResult.Status.SUCCESS.getVal()); // 0-成功
                    restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", null, Void.class, listResult);
                    long total = System.currentTimeMillis() - st;
                    System.out.println("共耗时：" + total / 1000 + "s");
                } catch (Exception e) {
                    e.printStackTrace();
                    List<String> elist = new ArrayList<>();
                    elist.add(e.getMessage());
                    listResult.setResult(elist);
                    listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 1-失败
                    restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", null, Void.class, listResult);
                }
            });
            t.start();
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("开始解析", ENTITY_NAME)).body(null);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", ex.getMessage())).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "解析失败")).body(null);
        }

    }

    /**
     * 解析单条数据区域的信息
     *
     * @param id
     * @return
     */

    @GetMapping("/reviseAreaInfo/{id}")
    @ApiOperation(value = "解析单条数据区域信息", notes = "解析单条数据区域信息")
    public ResponseEntity reviseAreaInfo(@PathVariable("id") String id) {

        try {
            //该了
            DataInfoExcel one = dataInfoExcelRepository.findOne(id);
            if (Objects.isNull(one)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "数据不存在")).body(null);
            }
            //获取家庭地址
            String homeAddress = one.getHomeAddress();
            //获取单位地址
            String workAddress = one.getCompanyAddr();
            //获取省份
            String province = one.getProvince();
            //获取城市
            String city = one.getCity();

            // 根据家庭地址/单位地址获取到正确的省份和城市
            String address = StringUtils.isNotBlank(homeAddress) ? homeAddress.trim() : workAddress.trim();
            if (StringUtils.isBlank(address)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "该条数据地址信息为空")).body(null);
            }

            // 解析
            AreaResult result = areaReviseDataService.getAreaByAddress(address);
            if (Objects.isNull(result)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "该条数据不能根据地址信息解析出区域信息！")).body(null);
            }
            if (Objects.equals(province, result.getProvince()) && Objects.equals(city, result.getCity())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "解析成功！")).body(null);
            }
            one.setProvince(result.getProvince());
            one.setCity(result.getCity());
            // 更新
            DataInfoExcel save = dataInfoExcelRepository.save(one);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "解析成功！")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "user", "系统异常！")).body(null);
        }
    }
}