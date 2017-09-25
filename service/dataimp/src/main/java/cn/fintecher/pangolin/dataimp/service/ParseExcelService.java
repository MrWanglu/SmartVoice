package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.DataImportRecord;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import cn.fintecher.pangolin.dataimp.util.ParseRowService;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sun on 2017/9/24.
 */

@Service
public class ParseExcelService {

    private final Logger logger = LoggerFactory.getLogger(ParseRowService.class);

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
                           List<TemplateExcelInfo> templateExcelInfos) throws Exception {
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
                        dataIndex);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        while (rowTotal != dataIndex.size()) {
            Thread.currentThread().sleep(1);
        }
        logger.debug("-----------------------{},{}",rowTotal,dataIndex.size());
        watch.stop();
        logger.debug("解析完成，共耗时{}ms", watch.getTotalTimeMillis());
        return;
    }
}
