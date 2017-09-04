package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.CaseInfoVerificationModel;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationService.java,v 0.1 2017/8/31 19:30 yuanyanting Exp $$
 */
@Service(value = "caseInfoVerificationService")
public class CaseInfoVerificationService {

    private final Logger logger = LoggerFactory.getLogger(CaseInfoVerificationService.class);

    @Autowired
    private RestTemplate restTemplate;

    public String exportCaseInfoVerification(List<CaseInfoVerificationModel> caseInfoVerificationList) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        Map<String,String> headMap = new LinkedHashMap<>(); // Excel头
        headMap.put("caseNumber","案件编号");
        headMap.put("personalName","客户姓名");
        headMap.put("personalMobileNo","手机号");
        headMap.put("personalIdCard","身份证号");
        headMap.put("batchNumber","批次号");
        headMap.put("overdueDays","逾期天数");
        headMap.put("overdueAmount","逾期总金额");
        headMap.put("principalName","委托方");
        headMap.put("assistCollector","催收员");
        headMap.put("collectionStatus","催收状态");
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String,Object> map;
            for (CaseInfoVerificationModel caseInfoVerification : caseInfoVerificationList) {
                map = new LinkedHashMap<>();
                map.put("caseNumber",caseInfoVerification.getCaseNumber());
                map.put("personalName",caseInfoVerification.getPersonalInfo().getName());
                map.put("personalMobileNo",caseInfoVerification.getPersonalInfo().getMobileNo());
                if (Objects.nonNull(caseInfoVerification.getPersonalInfo())) {
                    map.put("personalIdCard",caseInfoVerification.getPersonalInfo().getIdCard());
                }
                map.put("batchNumber",caseInfoVerification.getBatchNumber());
                map.put("overdueDays",caseInfoVerification.getOverdueDays());
                map.put("overdueAmount",caseInfoVerification.getOverdueAmount());
                if (Objects.nonNull(caseInfoVerification.getPrincipalId())) {
                    map.put("principalName",caseInfoVerification.getPrincipalId().getName());
                }
                if (Objects.nonNull(caseInfoVerification.getAssistCollector())) {
                    map.put("assistCollector",caseInfoVerification.getAssistCollector().getUserName());
                }
                map.put("collectionStatus",caseInfoVerification.getCollectionStatus());
                dataList.add(map);
            }
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("核销管理");
            ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "核销管理表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            return restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class).getBody();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
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
