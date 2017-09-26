package cn.fintecher.pangolin.dataimp.web;


import cn.fintecher.pangolin.dataimp.entity.*;
import cn.fintecher.pangolin.dataimp.model.DataInfoExcelFileExist;
import cn.fintecher.pangolin.dataimp.model.UpLoadFileModel;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelFileRepository;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelRepository;
import cn.fintecher.pangolin.dataimp.repository.RowErrorRepository;
import cn.fintecher.pangolin.dataimp.service.DataInfoExcelService;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 14:44 2017/7/18
 */
@RestController
@RequestMapping("/api/dataInfoExcelController")
@Api(description = "案件导入")
public class DataInfoExcelController {
    @Autowired
    DataInfoExcelService dataInfoExcelService;

    @Autowired
    DataInfoExcelRepository dataInfoExcelRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    DataInfoExcelFileRepository dataInfoExcelFileRepository;

    @Autowired
    RowErrorRepository rowErrorRepository;

    private final Logger logger = LoggerFactory.getLogger(DataInfoExcelController.class);
    private static final String ENTITY_NAME = "DataInfoExcel";

    @GetMapping("/getDataInfoExcelList")
    @ApiOperation(value = "案件导入分页查询", notes = "案件导入分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<DataInfoExcel>> getDataInfoExcelList(@QuerydslPredicate(root = DataInfoExcel.class) Predicate predicate,
                                                                    @ApiIgnore Pageable pageable,
                                                                    @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                                    @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        //只查询本人数据
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QDataInfoExcel.dataInfoExcel.companyCode.eq(companyCode));
            }
        } else {
            builder.and(QDataInfoExcel.dataInfoExcel.operator.eq(user.getId()));
            builder.and(QDataInfoExcel.dataInfoExcel.companyCode.eq(user.getCompanyCode()));
        }
        Page<DataInfoExcel> dataInfoExcelPage = dataInfoExcelRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询数据成功", ENTITY_NAME)).body(dataInfoExcelPage);
    }

    @PostMapping("/importExcelData")
    @ApiOperation(value = "案件导入", notes = "案件导入")
    public ResponseEntity<List<CellError>> importExcelData(@RequestBody DataImportRecord dataImportRecord,
                                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        // 超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(dataImportRecord.getCompanyCode())) {
                user.setCompanyCode(dataImportRecord.getCompanyCode());
            }
        }
        if (StringUtils.isBlank(dataImportRecord.getPrincipalId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择委托方!")).body(null);
        }
        try {
            dataInfoExcelService.importExcelData(dataImportRecord, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_NAME, "导入成功")).body(null);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getDataInfoExcelDetails")
    @ApiOperation(value = "案件详情查询操作", notes = "案件详情查询操作")
    public ResponseEntity<DataInfoExcel> getDataInfoExcelDetails(@RequestParam("id") String id) {
        DataInfoExcel dataInfoExcel = dataInfoExcelRepository.findOne(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(dataInfoExcel);
    }

    @GetMapping("/queryBatchNumGroup")
    @ApiOperation(value = "获取批次号列表", notes = "获取批次号列表")
    public ResponseEntity<List<String>> queryBatchNumGroup(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                           @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                user.setCompanyCode(companyCode);
            }
        }
        List<String> batchNumList = dataInfoExcelService.queryBatchNumGroup(user);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("获取批次号成功", ENTITY_NAME)).body(batchNumList);
    }

    @PostMapping("/uploadCaseFileSingle")
    @ResponseBody
    @ApiOperation(value = "导入单个案件附件", notes = "导入单个案件附件")
    public ResponseEntity uploadCaseFileSingle(@RequestBody UpLoadFileModel upLoadFileModel,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        //超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            user.setCompanyCode(upLoadFileModel.getCompanyCode());
        }
        dataInfoExcelService.uploadCaseFileSingle(upLoadFileModel, user);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("添加附件成功", ENTITY_NAME)).body(null);
    }

    @GetMapping("/getCaseFileList")
    @ApiOperation(value = "获取单个案件附件列表", notes = "获取单个案件附件列表")
    public ResponseEntity<List<DataInfoExcelFile>> getCaseFileList(@RequestParam(required = true) @ApiParam(value = "批次号") String batchNumber,
                                                                   @RequestParam(required = true) @ApiParam(value = "案件编号") String caseNUmber,
                                                                   @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        QDataInfoExcelFile qDataInfoExcelFile = QDataInfoExcelFile.dataInfoExcelFile;
        Iterable<DataInfoExcelFile> dataInfoExcelFileIterable = dataInfoExcelFileRepository.findAll(qDataInfoExcelFile.batchNumber.eq(batchNumber)
                .and(qDataInfoExcelFile.caseNumber.eq(caseNUmber)
                        .and(qDataInfoExcelFile.companyCode.eq(user.getCompanyCode())
                        )));
        List<DataInfoExcelFile> dataInfoExcelFileList = new ArrayList<>();
        dataInfoExcelFileIterable.forEach(single -> dataInfoExcelFileList.add(single));
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取附件列表", ENTITY_NAME)).body(dataInfoExcelFileList);
    }

    @GetMapping("/deleteCasesByBatchNum")
    @ApiOperation(value = "按批次号删除案件", notes = "按批次号删除案件")
    public ResponseEntity deleteCasesByBatchNum(@RequestParam(required = true) @ApiParam(value = "批次号") String batchNumber,
                                                @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                user.setCompanyCode(companyCode);
            }
        }
        dataInfoExcelService.deleteCasesByBatchNum(batchNumber, user);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("删除案件信息", ENTITY_NAME)).body(null);
    }

    @GetMapping("/checkCasesFile")
    @ResponseBody
    @ApiOperation(value = "检查案件附件是否存在", notes = "检查案件附件是否存在")
    public ResponseEntity<List<DataInfoExcelFileExist>> checkCasesFile(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                                       @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        ResponseEntity<User> userResponseEntity = null;
        try {

            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                user.setCompanyCode(companyCode);
            }
        }
        List<DataInfoExcelFileExist> checkStr = dataInfoExcelService.checkCasesFile(user);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("检查附件是否存在", ENTITY_NAME)).body(checkStr);
    }

    @GetMapping("/casesConfirmByBatchNum")
    @ApiOperation(value = "案件确认操作", notes = "案件确认操作")
    public ResponseEntity casesConfirmByBatchNum(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                 @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode,
                                                 @RequestParam(value = "batchNumber", required = true) @ApiParam("批次号") String batchNumber) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                user.setCompanyCode(companyCode);
            }
        }
        try {
            dataInfoExcelService.casesConfirmByBatchNum(user, batchNumber);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"", e.getMessage())).body(null);
        }
    }

    @GetMapping("/loadTemplate")
    @ApiOperation(value = "案件导入模板下载", notes = "案件导入模板下载")
    public ResponseEntity<String> loadTemplate(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("DataInfoExcelController", "loadTemplate", e.getMessage())).body(null);
        }
        User user = userResponseEntity.getBody();
        ResponseEntity<SysParam> forEntity = null;
        try {
            String requestUrl = Constants.SYSPARAM_URL.concat("?")
                    .concat("&userId=".concat(user.getId()))
                    .concat("&companyCode=".concat(user.getCompanyCode()))
                    .concat("&code=".concat(Constants.CASE_IMPORT_TEMPLATE_URL_CODE))
                    .concat("&type=".concat(Constants.CASE_IMPORT_TEMPLATE_URL_TYPE));
//            logger.info(requestUrl);
            forEntity = restTemplate.getForEntity(requestUrl, SysParam.class);
            SysParam body = forEntity.getBody();
            return ResponseEntity.ok().body(body.getValue());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("DataInfoExcelController", "loadTemplate", "下载失败")).body(null);
        }
    }

    @GetMapping("/findUpload")
    @ApiOperation(value = "查看附件", notes = "查看附件")
    public ResponseEntity<List<DataInfoExcelFile>> findUpload(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                              @RequestParam(value = "caseNumber", required = true) @ApiParam("案件编号") String caseNumber,
                                                              @RequestParam(value = "companyCode", required = false) String companyCode) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("DataInfoExcelController", "findUpload", e.getMessage())).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                user.setCompanyCode(companyCode);
            }
        }
        Iterable<DataInfoExcelFile> all = dataInfoExcelFileRepository.findAll(QDataInfoExcelFile.dataInfoExcelFile.caseNumber.eq(caseNumber)
                .and(QDataInfoExcelFile.dataInfoExcelFile.companyCode.eq(user.getCompanyCode())));
        List<DataInfoExcelFile> dataInfoExcelFiles = IterableUtils.toList(all);
        return ResponseEntity.ok().body(dataInfoExcelFiles);
    }

    @GetMapping("/findError")
    @ApiOperation(value = "查看批次错误报告", notes = "查看批次错误报告")
    public ResponseEntity<Page<RowError>> findError(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                             @RequestParam(value = "batchNumber", required = true) @ApiParam("批次号") String batchNumber,
                                                             @RequestParam(value = "companyCode", required = false) @ApiParam("批次号") String companyCode,
                                                             @ApiIgnore Pageable pageable) {
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        User user = userResponseEntity.getBody();
        try {
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    user.setCompanyCode(companyCode);
                }
            }
            QRowError qRowError = QRowError.rowError;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qRowError.batchNumber.eq(batchNumber));
            builder.and(qRowError.companyCode.eq(companyCode));
            Page<RowError> all = rowErrorRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(all);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "查看错误报告失败!")).body(null);
        }
    }
}
