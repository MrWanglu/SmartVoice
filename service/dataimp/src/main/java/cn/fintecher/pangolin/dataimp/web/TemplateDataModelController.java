package cn.fintecher.pangolin.dataimp.web;


import cn.fintecher.pangolin.dataimp.entity.QTemplateDataModel;
import cn.fintecher.pangolin.dataimp.entity.TemplateDataModel;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import cn.fintecher.pangolin.dataimp.repository.TemplateDataModelRepository;
import cn.fintecher.pangolin.dataimp.service.TemplateDataModelService;
import cn.fintecher.pangolin.dataimp.util.ExcelUtil;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by luqiang on 2017/7/25.
 */
@RestController
@RequestMapping("/api/accImportExcelDataController")
@Api(description = "excel模板信息操作")
public class TemplateDataModelController {
    @Autowired
    TemplateDataModelRepository templateDataModelRepository;
    @Autowired
    TemplateDataModelService templateDataModelService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger= LoggerFactory.getLogger(TemplateDataModelController.class);
    private static final String ENTITY_TEMPLATE = "template";
    private static final String ENTITY_NAME="TemplateDataModel";

    /**
     *
     * excel模板查询
     * @param pageable
     * @param token
     * @return
     */
    @RequestMapping(value = "/getExcelTemplateList", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "获取Excel模板分页查询", notes = "获取Excel模板分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getExcelTemplateList(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                               @QuerydslPredicate(root = TemplateDataModel.class) Predicate predicate,@RequestHeader(value = "X-UserToken") String token,
                                               @ApiIgnore Pageable pageable) {
        try {
            ResponseEntity<User> userResponseEntity=null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user",ENTITY_NAME)).body(null);
            }
            User user=userResponseEntity.getBody();
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("templateDataModel", "TemplateDataModel", "请选择公司")).body(null);
                }
                builder.and(QTemplateDataModel.templateDataModel.companyCode.eq(companyCode));
            }else{
                builder.and(QTemplateDataModel.templateDataModel.companyCode.eq(user.getCompanyCode()));
            }
            Page<TemplateDataModel> page = templateDataModelRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accImportExcelDataController/getExcelTemplateList");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    @DeleteMapping(value = "/deleteExcelData")
    @ApiOperation(value = "删除导入模板信息", notes = "删除导入模板信息")
    public ResponseEntity deleteExcelData(@RequestParam(required = true) String id) {
        try {
            if (Objects.nonNull(id)) {
                templateDataModelRepository.delete(id);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功",ENTITY_TEMPLATE)).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    /**
     * 上传excel文件并解析
     * @param id
     * @param rowNum
     * @param colNum
     * @param
     * @return
     */
    @GetMapping("/importExcelTemplate")
    @ResponseBody
    @ApiOperation(value = "上传excel文件并解析", notes = "上传excel文件并解析")
    public ResponseEntity importExcelData(@RequestParam(required = true) @ApiParam(value = "文件ID") String id, @RequestParam(required = true) @ApiParam(value = "行号") String rowNum, @RequestParam(required = true) @ApiParam(value = "列号") String colNum) {
        try {
            //查找上传文件
            UploadFile uploadFile = null;
            try {
              //  uploadFileResponseEntity = uploadFileClient.getUploadFile(fileId);
                ResponseEntity<UploadFile>  uploadFileResponseEntity =restTemplate.getForEntity(Constants.FILEID_SERVICE_URL+"uploadFile/".concat(id), UploadFile.class,id);
                if (!uploadFileResponseEntity.hasBody()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("获取上传文件失败", "")).body(null);
                } else {
                    uploadFile = uploadFileResponseEntity.getBody();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
            }
            try {
                List<TemplateExcelInfo> cellList = templateDataModelService.importExcelData(uploadFile.getLocalUrl(), rowNum, colNum);
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(cellList);
            } catch (IndexOutOfBoundsException e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("上传文件失败", "", e.getMessage())).body(null);
            } catch (NumberFormatException e) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取用户信息失败", "")).body(null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }
    @PostMapping("/importExcelTemplateData")
    @ResponseBody
    @ApiOperation(value = "新增Excel模板配置保存操作", notes = "新增Excel模板配置保存操作")
    public ResponseEntity importExcelTemplateData(@RequestBody TemplateDataModel excelTemplateData, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        try {
            excelTemplateData = (TemplateDataModel) EntityUtil.emptyValueToNull(excelTemplateData);
            ResponseEntity<User> userResponseEntity=null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user",ENTITY_NAME)).body(null);
            }
            User user=userResponseEntity.getBody();
            excelTemplateData.setOperatorTime(ZWDateUtil.getNowDateTime());
            excelTemplateData.setOperator(user.getUserName());
            excelTemplateData.setOperatorName(user.getRealName());
            excelTemplateData.setDataRowNum(String.valueOf(Integer.parseInt(excelTemplateData.getDataRowNum())-1));
            if(Objects.isNull(user.getCompanyCode())){//如果是超级管理员，code码为空
                excelTemplateData.setCompanyCode(null);
            }else{
                excelTemplateData.setCompanyCode(user.getCompanyCode());
            }
            String templateId = excelTemplateData.getId();
            if (ZWStringUtils.isEmpty(templateId)) {
                excelTemplateData.setId(null);
            }
            templateDataModelRepository.save(excelTemplateData);
            String message = "添加成功";
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }
    @GetMapping("/getExcelList")
    @ApiOperation(value = "获取Excel映射字段", notes = "获取Excel映射字段")
    public ResponseEntity getExcelList() {
        try {
            List<TemplateExcelInfo> cellList = templateDataModelService.getExcelList();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询模板形式成功",ENTITY_TEMPLATE)).body(cellList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            String messgae = "系统错误";
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(messgae);
        }
    }
    @GetMapping("/checkTemplateName")
    @ApiOperation(value = "检查模板名称是否存在重复", notes = "检查模板名称是否存在重复")
    public ResponseEntity checkTemplateName(@RequestParam(required = true) @ApiParam("模板名称") String templateName) {
        try {
            Query query = new Query();
            if (ZWStringUtils.isNotEmpty(templateName)) {
                query.addCriteria(Criteria.where("templateName").is(templateName));
            }
            List<TemplateDataModel> list = mongoTemplate.find(query, TemplateDataModel.class);
            if (Objects.nonNull(list) && list.size() == 0) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(null);
            } else {
                String message ="模板名称不能重复";
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(message);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }
    @GetMapping("/getExcelTempleByPrincipalName")
    @ApiOperation(value = "获取Excel模板", notes = "获取Excel模板")
    public ResponseEntity getExcelTempleByTemplateName(@RequestParam String principalName, @RequestHeader(value = "X-UserToken") String token) {
        ResponseEntity<User> userResponseEntity=null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user",ENTITY_NAME)).body(null);
        }
        User user=userResponseEntity.getBody();
        String companyCode = user.getCompanyCode();
        if (ZWStringUtils.isEmpty(principalName)) {
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("没有委托方信息",ENTITY_TEMPLATE)).body(null);
        }
        logger.debug("委托方编号为：{}", principalName);
        try {
            List<TemplateDataModel> templateDataModels = templateDataModelRepository.findTemplateByPrincipalNameAndCompanyCode(principalName,companyCode);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(templateDataModels);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }
    @PostMapping("/createExcelTemplate")
    @ApiOperation(value = "导入模板配置:确认并下载", notes = "导入模板配置:确认并下载")
    public ResponseEntity createExcelTemplate(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token, @RequestBody @ApiParam("配置项") Map<String, String[]> map) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
       try {
           ResponseEntity<User> userResponseEntity=null;
           try {
               userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
           }catch (Exception e){
               logger.error(e.getMessage(),e);
               return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user",ENTITY_NAME)).body(null);
           }
           if(!userResponseEntity.hasBody()){

           }
            String[] titleList = {};
            List<String[]> list = templateDataModelService.processData(map);
            titleList = templateDataModelService.CopyTheArray(titleList, list);
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            ExcelUtil.createExcel(workbook, sheet, new ArrayList(), titleList, null, 0, 0);
            workbook.write(out);
           String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(System.currentTimeMillis() + "Excel模板.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity(Constants.FILEID_SERVICE_URL+"uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(null);
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(url.getBody());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
           return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }
}
