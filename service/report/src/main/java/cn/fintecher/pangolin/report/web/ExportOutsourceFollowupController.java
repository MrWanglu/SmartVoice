package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.ExcelExportUtil;
import cn.fintecher.pangolin.report.mapper.QueryOutsourceFollowupMapper;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.report.service.FollowRecordExportService;
import cn.fintecher.pangolin.report.service.OutsourceFollowRecordExportService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @Author : huaynmin
 * @Description : 导出委外跟进记录
 * @Date : 2017/9/27.
 */
@RestController
@RequestMapping("/api/exportOutsourceFollowupController")
@Api(value = "ExportOutsourceFollowupController", description = "导出委外跟进记录")
public class ExportOutsourceFollowupController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(ExportOutsourceFollowupController.class);
    private static final String ENTITY_NAME = "ExportFollowupController";

    @Inject
    private OutsourceFollowRecordExportService outsourceFollowRecordExportService;
    @Inject
    private FollowRecordExportService followRecordExportService;
    @Inject
    private QueryOutsourceFollowupMapper queryOutsourceFollowupMapper;
    @Inject
    private RabbitTemplate rabbitTemplate;

    @PostMapping(value = "/exportOutsourceFollowupRecord")
    @ApiOperation(notes = "导出委外跟进记录", value = "导出委外跟进记录")
    public ResponseEntity<String> exportOutsourceFollowupRecord(@RequestBody ExportOutsourceFollowRecordParams exportOutsourceFollowRecordParams,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
            if (Objects.nonNull(user.getCompanyCode())) {
                exportOutsourceFollowRecordParams.setCompanyCode(user.getCompanyCode());
            }
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", e.getMessage())).body(null);
        }
        ResponseEntity<ItemsModel> entity = null;
        if(exportOutsourceFollowRecordParams.getType()==0){
            entity = restTemplate.getForEntity("http://business-service/api/exportItemResource/getOutsourceExportItems?token="+token, ItemsModel.class);
        }else{
            entity = restTemplate.getForEntity("http://business-service/api/exportItemResource/getOutsourceFollowUpExportItems?token="+token, ItemsModel.class);
        }
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
        exportOutsourceFollowRecordParams.setExportItemList(items);
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
                    List<ExcportOutsourceResultModel> all = null;
                    if(exportOutsourceFollowRecordParams.getType()==0){
                        all = queryOutsourceFollowupMapper.findOutsourceRecord(exportOutsourceFollowRecordParams);
                    }else {
                        all = queryOutsourceFollowupMapper.findOutsourceFollowupRecord(exportOutsourceFollowRecordParams);
                    }
                    ResponseEntity<String> url = null;
                    if (!all.isEmpty()) {
                        progressMessage.setCurrent(2);
                        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        int maxNum = outsourceFollowRecordExportService.getMaxNum(all);
                        List<FollowupExportModel> dataList = null;
                        if (exportOutsourceFollowRecordParams.getType()==0){
                            dataList = outsourceFollowRecordExportService.getOutsourceRecordData(all);
                        }else {
                            dataList = outsourceFollowRecordExportService.getFollowupData(all);
                        }
                        String[] title = followRecordExportService.getTitle(exportOutsourceFollowRecordParams.getExportItemList(), maxNum);
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
                        url = restTemplate.postForEntity(Constants.UPLOAD_FILE_URL, param, String.class);
                    }
                    if (url == null && !all.isEmpty()) {
                        List<String> urls = new ArrayList<>();
                        ListResult listResult = new ListResult();
                        listResult.setUser(userId);
                        urls.add("导出失败！");
                        listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 1-失败
                        restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                        progressMessage.setCurrent(5);
                        rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                    } else {
                        if (all.isEmpty()) {
                            List<String> urls = new ArrayList<>();
                            ListResult listResult = new ListResult();
                            urls.add("要导出的数据为空");
                            listResult.setUser(userId);
                            listResult.setResult(urls);
                            listResult.setStatus(ListResult.Status.FAILURE.getVal()); // 0-成功
                            restTemplate.postForEntity("http://reminder-service/api/listResultMessageResource", listResult, Void.class);
                            progressMessage.setCurrent(5);
                            rabbitTemplate.convertAndSend(Constants.FOLLOWUP_EXPORT_QE, progressMessage);
                        } else {
                            List<String> urls = new ArrayList<>();
                            ListResult listResult = new ListResult();
                            urls.add(url.getBody());
                            log.info(url.getBody());
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
                    List<String> urls = new ArrayList<>();
                    ListResult listResult = new ListResult();
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

}
