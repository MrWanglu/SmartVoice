package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationRepository;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.CaseInfoVerification;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationService.java,v 0.1 2017/8/31 19:30 yuanyanting Exp $$
 */
@Service(value = "caseInfoVerificationService")
public class CaseInfoVerificationService {

    private final Logger logger = LoggerFactory.getLogger(CaseInfoVerificationService.class);

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private EntityManager em;

    public String exportCaseInfoVerification(List<CaseInfoVerification> caseInfoVerificationList) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        Map<String, String> headMap = new LinkedHashMap<>(); // Excel头
        headMap.put("caseNumber", "案件编号");
        headMap.put("personalName", "客户姓名");
        headMap.put("personalMobileNo", "手机号");
        headMap.put("personalIdCard", "身份证号");
        headMap.put("batchNumber", "批次号");
        headMap.put("overdueDays", "逾期天数");
        headMap.put("overdueAmount", "逾期总金额");
        headMap.put("principalName", "委托方");
        headMap.put("assistCollector", "催收员");
        headMap.put("collectionStatus", "催收状态");
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            Map<String, Object> map;
            for (CaseInfoVerification caseInfoVerification : caseInfoVerificationList) {
                map = new LinkedHashMap<>();
                map.put("caseNumber", caseInfoVerification.getCaseNumber());
                map.put("personalName", caseInfoVerification.getPersonalInfo().getName());
                map.put("personalMobileNo", caseInfoVerification.getPersonalInfo().getMobileNo());
                if (Objects.nonNull(caseInfoVerification.getPersonalInfo())) {
                    map.put("personalIdCard", caseInfoVerification.getPersonalInfo().getIdCard());
                }
                map.put("batchNumber", caseInfoVerification.getBatchNumber());
                map.put("overdueDays", caseInfoVerification.getOverdueDays());
                map.put("overdueAmount", caseInfoVerification.getOverdueAmount());
                if (Objects.nonNull(caseInfoVerification.getPrincipalId())) {
                    map.put("principalName", caseInfoVerification.getPrincipalId().getName());
                }
                if (Objects.nonNull(caseInfoVerification.getAssistCollector())) {
                    map.put("assistCollector", caseInfoVerification.getAssistCollector().getUserName());
                }
                map.put("collectionStatus", caseInfoVerification.getCollectionStatus());
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
            logger.error(e.getMessage(), e);
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

    /**
     * @Description 查询报表
     */
    public List<CaseInfoVerModel> getList(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        StringBuilder queryCon = new StringBuilder("select count(a.id) as cityCount,b.area_name as city,sum(a.overdue_amount) as amount from case_info_verification a LEFT JOIN area_code b " +
                "on a.area_id = b.id where 1=1");
        //公司code码
        if (StringUtils.isNotBlank(caseInfoVerificationParams.getCompanyCode())) {
            queryCon.append(" and a.company_code = ").append(caseInfoVerificationParams.getCompanyCode());
        } else {
            queryCon.append(" and a.company_code = ").append("'").append(tokenUser.getCompanyCode()).append("'");
        }
        //开始时间
        if (StringUtils.isNotBlank(caseInfoVerificationParams.getStartTime())) {
            queryCon.append(" and a.case_follow_in_time >= ").append("'").append(caseInfoVerificationParams.getStartTime()).append("'");
        }
        //结束时间
        if (StringUtils.isNotBlank(caseInfoVerificationParams.getEndTime())) {
            queryCon.append(" and a.case_follow_in_time <= ").append("'").append(caseInfoVerificationParams.getEndTime()).append("'");
        }
        queryCon.append(" group by b.area_name");
        logger.debug(queryCon.toString());
        List<Object[]> objects = em.createNativeQuery(queryCon.toString()).getResultList();
        List<CaseInfoVerModel> caseInfoVerModels = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object[] obj : objects) {
                CaseInfoVerModel caseInfoVerModel = new CaseInfoVerModel();
                caseInfoVerModel.setCityCount(Integer.parseInt(checkValueIsNull(obj[0])));
                caseInfoVerModel.setCity(checkValueIsNull(obj[1]));
                caseInfoVerModel.setAmount(new BigDecimal(checkValueIsNull(obj[2])));
                caseInfoVerModel.setAmountStr(caseInfoVerModel.getAmount().toString());
                caseInfoVerModels.add(caseInfoVerModel);
            }
        }
        return caseInfoVerModels;
    }

    /**
     * 检查对象是否为字符串null
     *
     * @param obj
     * @return
     */
    private String checkValueIsNull(Object obj) {
        if (Objects.nonNull(obj)) {
            return String.valueOf(obj.equals("null") ? "" : obj);
        } else {
            return null;
        }

    }

    public String exportReport(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        workbook = new HSSFWorkbook();
        try {
            List<CaseInfoVerModel> caseInfoVerModelList = getList(caseInfoVerificationParams, tokenUser);
            HSSFSheet sheet = workbook.createSheet("核销管理报表");
            String[] titleList = {"区域", "累计金额", "累计户数"};
            String[] proNames = {"city", "amountStr", "cityCount"};
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
            return url;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
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
