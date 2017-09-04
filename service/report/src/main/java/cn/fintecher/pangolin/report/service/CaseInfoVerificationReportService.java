package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.report.mapper.CaseInfoVerMapper;
import cn.fintecher.pangolin.report.model.CaseInfoVerModel;
import cn.fintecher.pangolin.report.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.web.HeaderUtil;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationService.java,v 0.1 2017/8/31 19:30 yuanyanting Exp $$
 */
@Service(value = "caseInfoVerificationService")
public class CaseInfoVerificationReportService {

    private final Logger logger = LoggerFactory.getLogger(CaseInfoVerificationReportService.class);

    @Autowired
    private CaseInfoVerMapper caseInfoVerMapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询核销管理报表
     */
    public List<CaseInfoVerModel> getVerificationReport(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        if (Objects.isNull(tokenUser.getCompanyCode())) {
            return caseInfoVerMapper.getCaseInfoVerReport(caseInfoVerificationParams.getStartTime(), caseInfoVerificationParams.getEndTime(),
                    caseInfoVerificationParams.getPage(), caseInfoVerificationParams.getSize(), caseInfoVerificationParams.getCompanyCode());
        } else {
            return caseInfoVerMapper.getCaseInfoVerReport(caseInfoVerificationParams.getStartTime(), caseInfoVerificationParams.getEndTime(),
                    caseInfoVerificationParams.getPage(), caseInfoVerificationParams.getSize(), tokenUser.getCompanyCode());
        }
    }

    /**
     * 导出核销管理报表
     */
    public ResponseEntity<String> exportReport(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        List<CaseInfoVerModel> caseInfoVerModelList = getVerificationReport(caseInfoVerificationParams, tokenUser);
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        try {
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("核销管理报表");
            String[] titleList = {"区域", "累计金额", "累计户数"};
            String[] proNames = {"city", "amount", "cityCount"};
            ExcelUtil.createExcel(workbook, sheet, caseInfoVerModelList, titleList, proNames, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "核销报表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            String url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class).getBody();
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoVerificationModel", "", "上传服务器失败")).body(null);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoVerificationModel", "", "上传服务器失败")).body(url);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoVerificationModel", "", "上传服务器失败")).body(null);
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
}
