package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.DataImportRecord;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sun on 2017/9/24.
 */

@Service
public class ParseExcelService {

    private final Logger logger = LoggerFactory.getLogger(ParseExcelService.class);

    private final static List<String> isNeedTitle = new ArrayList<>();

    static {
        isNeedTitle.add("客户姓名");
        isNeedTitle.add("身份证号");
        isNeedTitle.add("逾期总金额(元)");
    }

    @Autowired
    private ParseExcelTask parseExcelTask;

    CopyOnWriteArrayList<Integer> dataIndex = new CopyOnWriteArrayList<>();

    public Sheet getExcelSheets(UploadFile file) {
        try {
            Workbook workbook = getWorkbook(file);
            Sheet sheetAt = workbook.getSheetAt(0);
            return sheetAt;
        } catch (Exception e) {
            throw new RuntimeException("获取Excel对象错误");
        }
    }

    private Workbook getWorkbook(UploadFile file) {
        Workbook workbook = null;
        InputStream inputStream = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(file.getLocalUrl(), HttpMethod.GET, new HttpEntity<byte[]>(headers), byte[].class);
            byte[] result = response.getBody();
            inputStream = new ByteArrayInputStream(result);
            String fileType = file.getType();
            if (Constants.EXCEL_TYPE_XLS.equals(fileType)) {
                workbook = new HSSFWorkbook(new POIFSFileSystem(inputStream));//支持低版本的Excel文件
            } else if (Constants.EXCEL_TYPE_XLSX.equals(fileType)) {
                workbook = new XSSFWorkbook(inputStream);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("获取Excel对象错误");
        } finally {
            closeWorkBook(workbook);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        }
        return workbook;
    }

    private void closeWorkBook(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Map<Integer, String> parseExcelHeader(int startCol, Row titleRow) throws Exception {
        try {
            int lastCellNum = titleRow.getLastCellNum();
            if (startCol > lastCellNum) {
                throw new Exception("Excel数据模板开始列大于数据总列数");
            }
            Map<Integer, String> headerMap = new LinkedHashMap<>();
            for (int columnIndex = startCol; columnIndex < titleRow.getLastCellNum(); columnIndex++) {
                Cell cell = titleRow.getCell(columnIndex);
                headerMap.put(columnIndex, cell.getStringCellValue());
            }
            return headerMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("解析Excel表头信息失败");
        }
    }

    public void parseSheet(Sheet sheet, int startRow, int startCol, Class<?> dataClass, DataImportRecord dataImportRecord,
                           List<TemplateExcelInfo> templateExcelInfos, CopyOnWriteArrayList<DataInfoExcel> dataInfoExcelList,
                           CopyOnWriteArrayList<RowError> forceErrorList, CopyOnWriteArrayList<RowError> promptErrorList) throws Exception {
        //获取每个sheet页的头部信息,用于和实体属性匹配(默认模板使用)
        Map<Integer, String> headerMap = new LinkedHashMap<>();
        //数据开始列
        int dataStartRow = 0;
        //默认数据模板导入
        if (Objects.isNull(templateExcelInfos)) {
            dataStartRow = startRow + 1;
            //读取该sheet页的标题信息
            Row titleRow = sheet.getRow(startRow);
            headerMap = parseExcelHeader(startCol, titleRow);
        } else {
            //走配置模板
            dataStartRow = startRow;
        }
        //循环解析sheet每行的数据
        int rowTotal = sheet.getLastRowNum();
        dataIndex.clear();
        logger.debug("解析数据开始...");
        StopWatch watch = new StopWatch();
        watch.start();
        for (int rowIndex = dataStartRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            RowError rowError = new RowError();
            try {
                //获取每一行的数据
                Row dataRow = sheet.getRow(rowIndex);
                if (dataRow == null) {
                    dataIndex.add(rowIndex);
                    continue;
                }
                parseExcelTask.parseRow(dataClass, dataRow, startCol, headerMap, rowError, dataImportRecord, rowIndex, templateExcelInfos,
                        dataIndex, dataInfoExcelList, forceErrorList, promptErrorList);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        while (rowTotal != dataIndex.size()) {
        }
        logger.debug("-----------------------{},{}",rowTotal,dataIndex.size());
        watch.stop();
        logger.debug("解析完成，共耗时{}ms", watch.getTotalTimeMillis());
        return;
    }

    public boolean checkHeader(Sheet sheet, int startRow, int startCol) {
        Row titleRow = sheet.getRow(startRow);
        int lastCellNum = titleRow.getLastCellNum();
        if (startCol > lastCellNum) {
            throw new RuntimeException("Excel数据模板开始列大于数据总列数");
        }
        List<String> title = new ArrayList<>();
        for (int columnIndex = startCol; columnIndex < titleRow.getLastCellNum(); columnIndex++) {
            Cell cell = titleRow.getCell(columnIndex);
            if (ZWStringUtils.isNotEmpty(cell.getStringCellValue())) {
                title.add(cell.getStringCellValue());
            }
        }
        if (title.containsAll(isNeedTitle)) {
            return true;
        }
        return false;
    }
}
