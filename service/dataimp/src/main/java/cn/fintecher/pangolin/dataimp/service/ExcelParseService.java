package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.ExcelSheetObj;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
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
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sunyanping on 2017/9/25.
 */
@Service
public class ExcelParseService {

    private final Logger logger = LoggerFactory.getLogger(ExcelParseService.class);

    @Autowired
    private ExcelParseAsyncTask excelParseAsyncTask;

    CopyOnWriteArrayList<Integer> dataIndex = new CopyOnWriteArrayList<>();

    int lastRowNum = 0;

    /**
     * 解析单sheet页的Excel
     *
     * @param file
     * @param dataClass
     * @param startRow
     * @param startCol
     * @return
     */
    public ExcelSheetObj parseExcelSingle(UploadFile file, Class<?>[] dataClass, int[] startRow, int[] startCol,
                                                 List<TemplateExcelInfo> templateExcelInfos) {
        logger.info("线程 {} 开始解析Excel..............................................", Thread.currentThread());
        StopWatch watch = new StopWatch();
        watch.start();
        Workbook workbook = null;
        ExcelSheetObj excelSheetObj = null;
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
            LinkedHashMap<String, Sheet> sheetLinkedHashMap = getExcelSheets(workbook);
            Iterator<Map.Entry<String, Sheet>> iterator = sheetLinkedHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                String sheetName = (String) entry.getKey();
                Sheet sheet = (Sheet) entry.getValue();
                try {
                    logger.info("开始解析 {} 页数据", sheetName);
                    excelSheetObj = parseSheet(sheet, startRow[0], startCol[0], dataClass[0], templateExcelInfos);
                    logger.info("结束解析 {} 页数据", sheetName);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
        watch.stop();
        logger.info("线程 {} 结束Excel解析,耗时：{} 毫秒", Thread.currentThread(), watch.getTotalTimeMillis());
        return excelSheetObj;
    }

    public LinkedHashMap<String, Sheet> getExcelSheets(Workbook workbook) {
        LinkedHashMap<String, Sheet> excelSheets = new LinkedHashMap<>();
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            String sheetName = workbook.getSheetName(sheetIndex);
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            excelSheets.put(sheetName, sheet);
            break;
        }
        if (excelSheets.isEmpty()) {
            excelSheets = null;
        }
        return excelSheets;
    }

    /**
     * 解析sheet页数据
     *
     * @param sheet
     * @param startRow
     * @param startCol
     * @param dataClass 接收数据的实体对象
     * @return 返回当前sheet的数据
     */
    public ExcelSheetObj parseSheet(Sheet sheet, int startRow, int startCol, Class<?> dataClass,
                                           List<TemplateExcelInfo> templateExcelInfos) throws Exception {
        ExcelSheetObj excelSheetObj = new ExcelSheetObj();  //Excel sheet页对象信息
        List dataList = new ArrayList();     //sheet页数据
        List<RowError> sheetErrorList = new ArrayList<>();  //sheet页错误信息
        String sheetName = sheet.getSheetName();  //sheet名称
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
        int threadParseNum = 5;
        int lastRowNum = sheet.getLastRowNum() -1; //数据总行数
        this.lastRowNum = lastRowNum;
        int threadNum = getThreadNum(lastRowNum, threadParseNum); //要启用的线程数
        int rowNullNum = 0;

        logger.info("共{}条数据,开始解析...", lastRowNum);
        StopWatch watch = new StopWatch();
        watch.start();
        for (int i = 0; i < threadNum; i++) {
            //线程开始解析的行
            int istartNum = dataStartRow;
            if (i == 0) {
                istartNum = 1;
            } else {
                istartNum = (i * threadParseNum) + 1;
            }
            excelParseAsyncTask.parseRowDataExcel(istartNum,threadParseNum,startCol,sheet,dataClass,headerMap,dataList,sheetErrorList, rowNullNum, templateExcelInfos);
        }
        excelSheetObj.setSheetName(sheetName);
        while (this.lastRowNum != (dataList.size() + rowNullNum)) {
            Thread thread = Thread.currentThread();
            thread.sleep(1);
        }
        watch.stop();
        logger.info("共{}条可读数据,解析完成,共耗时{}ms", dataList.size(), watch.getTotalTimeMillis());
        excelSheetObj.setDataList(dataList);
        excelSheetObj.setSheetErrorList(sheetErrorList);
        return excelSheetObj;
    }

    /**
     * 关闭文件流
     *
     * @param workbook
     * @throws Exception
     */
    private void closeWorkBook(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Map<Integer, String> parseExcelHeader(int startCol, Row titleRow) throws Exception {
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

    private static int getThreadNum(int lastRowNum, int rowNum) {
        int a = lastRowNum / rowNum;
        int b = lastRowNum % rowNum;
        if (b == 0) {
            return a == 0 ? 1 : a;
        } else {
            return a == 0 ? 1 : a + 1;
        }
    }
}
