package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.QueryFollowupMapper;
import cn.fintecher.pangolin.report.model.ExcportResultModel;
import cn.fintecher.pangolin.report.model.ExportFollowRecordParams;
import cn.fintecher.pangolin.report.model.ExportFollowupModel;
import cn.fintecher.pangolin.report.model.ExportFollowupParams;
import cn.fintecher.pangolin.report.service.ExportFollowupService;
import cn.fintecher.pangolin.report.service.FollowRecordExportService;
import cn.fintecher.pangolin.report.util.ExcelExportHelper;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @Author : baizhangyu
 * @Description : 导出跟进记录
 * @Date : 2017/9/6.
 */
@RestController
@RequestMapping("/api/exportFollowupController")
@Api(value = "ExportFollowupController", description = "导出跟进记录")
public class ExportFollowupController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(ExportFollowupController.class);

    @Autowired
    private ExportFollowupService exportFollowupService;
    @Inject
    private FollowRecordExportService followRecordExportService;
    @Inject
    QueryFollowupMapper queryFollowupMapper;

    @PostMapping (value = "/getExcelData")
    @ApiOperation(value = "导出跟进记录",notes = "导出跟进记录")
    public ResponseEntity getExcelData(@RequestBody ExportFollowupModel exportFollowupModel,
                                       @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token){
        try {
            XSSFWorkbook workbook = null;
            File file = null;
            ByteArrayOutputStream out = null;
            FileOutputStream fileOutputStream = null;

            User user = null;
            try {
                user = getUserByToken(token);
            } catch (final Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", e.getMessage())).body(null);
            }
            try {
                String companyCode = user.getCompanyCode();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (StringUtils.isNotBlank(exportFollowupModel.getCompanyCode())) {
                        companyCode = exportFollowupModel.getCompanyCode();
                    }
                }
                List<String> caseNumberList = exportFollowupModel.getList();
                if (caseNumberList.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择案件!")).body(null);
                }
                List<ExportFollowupParams> caseFollowupRecords = exportFollowupService.getExcelData(caseNumberList,companyCode);
                if (caseFollowupRecords.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "要导出的跟进记录数据为空!")).body(null);
                }
                if (caseFollowupRecords.size() > 10000) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
                }
                workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("sheet1");
                out = new ByteArrayOutputStream();
                Map<String, String> head = followRecordExportService.createHead();
                List<Map<String, Object>> data = followRecordExportService.createData(caseFollowupRecords);
                ExcelExportHelper.createExcel(workbook, sheet, head, data, 0, 0);
                workbook.write(out);
                String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xlsx");
                file = new File(filePath);
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(out.toByteArray());
                FileSystemResource resource = new FileSystemResource(file);
                MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                param.add("file", resource);
                ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
                if (url == null) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "系统错误!")).body(null);
                } else {
                    return ResponseEntity.ok().body(url.getBody());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error(ex.getMessage());
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "上传文件服务器失败")).body(null);
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
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ExportFollowupController","ExportFollowupController","导出跟进记录失败!")).body(null);
        }
    }

    @PostMapping (value = "/test")
    public ResponseEntity getExcelData(@RequestBody ExportFollowRecordParams exportFollowupParams) {
        long l = System.currentTimeMillis();
        List<ExcportResultModel> test = queryFollowupMapper.findFollowupRecord(exportFollowupParams);
        long l1 = System.currentTimeMillis();
        log.info("耗时-----------------------------------"+(l1-l));
        return ResponseEntity.ok(test);
    }

}
