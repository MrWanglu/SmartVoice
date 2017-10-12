package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.ReminderMode;
import cn.fintecher.pangolin.entity.ReminderType;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.ExcelExportUtil;
import cn.fintecher.pangolin.report.mapper.QueryFollowupMapper;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.report.service.ExportFollowupService;
import cn.fintecher.pangolin.report.service.FollowRecordExportService;
import cn.fintecher.pangolin.report.util.ExcelExportHelper;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.*;


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
    private static final String ENTITY_NAME = "ExportFollowupController";

    @Autowired
    private ExportFollowupService exportFollowupService;
    @Inject
    private FollowRecordExportService followRecordExportService;
    @Inject
    private QueryFollowupMapper queryFollowupMapper;
    @Inject
    private RabbitTemplate rabbitTemplate;

    @PostMapping(value = "/getExcelData")
    @ApiOperation(value = "导出跟进记录", notes = "导出跟进记录")
    public ResponseEntity getExcelData(@RequestBody ExportFollowupModel exportFollowupModel,
                                       @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
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
                List<ExportFollowupParams> caseFollowupRecords = exportFollowupService.getExcelData(caseNumberList, companyCode);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ExportFollowupController", "ExportFollowupController", "导出跟进记录失败!")).body(null);
        }
    }

    @PostMapping(value = "/exportFollowupRecord")
    @ApiOperation(notes = "导出跟进记录", value = "导出跟进记录")
    public ResponseEntity exportFollowupRecord(@RequestBody(required = false) ExportFollowRecordParams exportFollowupParams,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {

        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", e.getMessage())).body(null);
        }
        exportFollowupParams.setCompanyCode(user.getCompanyCode());
        ResponseEntity<ItemsModel> entity = restTemplate.getForEntity("http://business-service/api/exportItemResource/getExportItems?token="+token, ItemsModel.class);
        ItemsModel itemsModel = entity.getBody();
        if(itemsModel.getPersonalItems().isEmpty() && itemsModel.getJobItems().isEmpty() && itemsModel.getConnectItems().isEmpty()
                && itemsModel.getCaseItems().isEmpty() && itemsModel.getBankItems().isEmpty() && itemsModel.getFollowItems().isEmpty()){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先设置导出项")).body(null);
        }
        List<String> items = new ArrayList<>();
        items.addAll(itemsModel.getPersonalItems());
        items.addAll(itemsModel.getJobItems());
        items.addAll(followRecordExportService.parseConnect(itemsModel.getConnectItems()));
        items.addAll(itemsModel.getCaseItems());
        items.addAll(itemsModel.getBankItems());
        items.addAll(followRecordExportService.parseFollow(itemsModel.getFollowItems()));
        exportFollowupParams.setExportItemList(items);

        final String userId = user.getId();
        try {
            //创建一个线程，执行导出任务
            Thread t = new Thread(() -> {
                SXSSFWorkbook workbook = null;
                File file = null;
                ByteArrayOutputStream out = null;
                FileOutputStream fileOutputStream = null;
                ProgressMessage progressMessage = null;
                try {
                    progressMessage = new ProgressMessage();
                    // 登录人ID
                    progressMessage.setUserId(userId);
                    //要解析的总数据
                    progressMessage.setTotal(5);
                    //当前解析的数据
                    progressMessage.setCurrent(0);
                    //正在处理数据
                    progressMessage.setText("正在处理数据");
                    rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                    List<ExcportResultModel> all = new ArrayList<>();
                    if(Objects.equals(exportFollowupParams.getType(),1)) {
                        all = queryFollowupMapper.findFollowupRecord(exportFollowupParams);
                    }else{
                        all = queryFollowupMapper.findCollingFollowupRecord(exportFollowupParams);
                    }
                    ResponseEntity<String> url = null;
                    if (!all.isEmpty()) {
                        progressMessage.setCurrent(2);
                        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        List<FollowupExportModel> dataList = followRecordExportService.getFollowupData(all);
                        int maxNum = followRecordExportService.getMaxNum(all);
                        String[] title = followRecordExportService.getTitle(exportFollowupParams.getExportItemList(), maxNum);
                        Map<String, String> headMap = ExcelExportUtil.createHeadMap(title, FollowupExportModel.class);
                        progressMessage.setCurrent(3);
                        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        workbook = new SXSSFWorkbook(5000);
                        ExcelExportUtil.createExcelData(workbook, headMap, dataList, 1048575);
                        out = new ByteArrayOutputStream();
                        workbook.write(out);
                        String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xlsx");
                        file = new File(filePath);
                        fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(out.toByteArray());
                        FileSystemResource resource = new FileSystemResource(file);
                        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                        param.add("file", resource);
                        url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
                    }
                    if (url == null && !all.isEmpty()) {
                        ListResult listResult = new ListResult();
                        listResult.setUser(userId);
                        List<String> urls = new ArrayList<>();
                        urls.add("导出失败！");
                        listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 1-失败
                        restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                        progressMessage.setCurrent(5);
                        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                    } else {
                        if (all.isEmpty()) {
                            ListResult listResult = new ListResult();
                            List<String> urls = new ArrayList<>();
                            urls.add("要导出的数据为空");
                            listResult.setUser(userId);
                            listResult.setResult(urls);
                            listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 0-成功
                            restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                            progressMessage.setCurrent(5);
                            rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        } else {
                            ListResult listResult = new ListResult();
                            List<String> urls = new ArrayList<>();
                            urls.add(url.getBody());
                            listResult.setUser(userId);
                            listResult.setResult(urls);
                            listResult.setStatus(ListResult.Status.SUCCESS.getVal()); // 0-成功
                            restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                            progressMessage.setCurrent(5);
                            rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ListResult listResult = new ListResult();
                    List<String> urls = new ArrayList<>();
                    urls.add("导出失败！");
                    listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 1-失败
                    restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                    progressMessage.setCurrent(5);
                    rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
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
            });
            t.start();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("开始导出,完成后请前往消息列表查看下载。", "")).body(null);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "上传文件服务器失败")).body(null);
        }
    }

    private void sendReminder(String title, String content, String userId) {
        SendReminderMessage sendReminderMessage = new SendReminderMessage();
        sendReminderMessage.setTitle(title);
        sendReminderMessage.setContent(content);
        sendReminderMessage.setType(ReminderType.FOLLOWUP_EXPORT);
        sendReminderMessage.setMode(ReminderMode.POPUP);
        sendReminderMessage.setCreateTime(new Date());
        sendReminderMessage.setUserId(userId);
        //发送消息
        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, sendReminderMessage);
    }
}
