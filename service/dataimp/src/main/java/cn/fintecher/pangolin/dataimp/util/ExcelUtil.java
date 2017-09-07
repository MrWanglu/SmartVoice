package cn.fintecher.pangolin.dataimp.util;

import cn.fintecher.pangolin.dataimp.annotation.ExcelAnno;
import cn.fintecher.pangolin.dataimp.entity.CellError;
import cn.fintecher.pangolin.dataimp.entity.ExcelSheetObj;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @Author: PeiShouWen
 * @Description: Excel读写操作
 * @Date 11:26 2017/3/3
 */
public class ExcelUtil {
    private final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);


    /**
     * 解析单sheet页的Excel
     *
     * @param file
     * @param dataClass
     * @param startRow
     * @param startCol
     * @return
     */
    public static ExcelSheetObj parseExcelSingle(UploadFile file, Class<?>[] dataClass, int[] startRow, int[] startCol,
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


    /**
     * 关闭文件流
     *
     * @param workbook
     * @throws Exception
     */
    private static void closeWorkBook(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 获取Excel中的每个sheet页，
     * 直接解析第一个sheet也页数据
     *
     * @param workbook
     * @return 返回按Excel实际顺序的sheet对象
     */
    public static LinkedHashMap<String, Sheet> getExcelSheets(Workbook workbook) {
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
    public static ExcelSheetObj parseSheet(Sheet sheet, int startRow, int startCol, Class<?> dataClass,
                                           List<TemplateExcelInfo> templateExcelInfos) throws Exception {
        ExcelSheetObj excelSheetObj = new ExcelSheetObj();
        List objList = new ArrayList();
        List<CellError> cellErrorList = new ArrayList<>();
        String sheetName = sheet.getSheetName();
        //获取每个sheet页的头部信息,用于和实体属性匹配(默认模板使用)
        Map<Integer, String> headerMap = null;
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
        //解析数据行
        for (int rowIndex = dataStartRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            try {
                //获取每一行的数据
                Row dataRow = sheet.getRow(rowIndex);
                if (!isBlankRow(dataRow)) {
                    //解析一行中每一列数据
                    Object obj = parseRow(dataClass, dataRow, startCol, headerMap, cellErrorList, sheetName, rowIndex, templateExcelInfos);
                    if (null != obj) {
                        objList.add(obj);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        excelSheetObj.setSheetName(sheetName);
        excelSheetObj.setDatasList(objList);
        excelSheetObj.setCellErrorList(cellErrorList);
        return excelSheetObj;
    }


    /**
     * @param dataClass  数据实体
     * @param dataRow    数据行
     * @param startCol   开始列
     * @param headerMap  头部信息
     * @param cellErrors 错误信息
     * @param sheetName
     * @param rowIndex   行数
     * @return 返回实体对象
     */
    public static Object parseRow(Class<?> dataClass, Row dataRow, int startCol, Map<Integer, String> headerMap, List<CellError> cellErrors,
                                  String sheetName, int rowIndex, List<TemplateExcelInfo> templateExcelInfos) {
        //反射创建实体对象
        Object obj = null;
        try {
            obj = dataClass.newInstance();
            //默认数据模板
            if (Objects.isNull(templateExcelInfos)) {
                for (int colIndex = startCol; colIndex < dataRow.getLastCellNum(); colIndex++) {
                    //获取该列对应的头部信息中文
                    String titleName = headerMap.get(colIndex);
                    Cell cell = dataRow.getCell(colIndex);
                    matchFields(dataClass, dataRow, cellErrors, sheetName, rowIndex, obj, colIndex, titleName, cell);
                }
            } else {
                //配置模板
                for (TemplateExcelInfo templateExcelInfo : templateExcelInfos) {
                    if (StringUtils.isNotBlank(templateExcelInfo.getRelateName())) {
                        Cell cell = dataRow.getCell(templateExcelInfo.getCellNum());
                        if (cell != null && !cell.toString().trim().equals("")) {
                            //获取类中所有的字段
                            Field[] fields = dataClass.getDeclaredFields();
                            for (Field field : fields) {
                                //实体中的属性名称
                                String proName = field.getName();
                                //匹配到实体中相应的字段
                                if (proName.equals(templateExcelInfo.getRelateName())) {
                                    //打开实体中私有变量的权限
                                    field.setAccessible(true);
                                    //实体中变量赋值
                                    try {
                                        field.set(obj, getObj(field.getType(), cell));
                                    } catch (Exception e) {
                                        String errorMsg = "第[" + dataRow.getRowNum() + "]行，字段:[" + proName + "]的数据类型不正确";
                                        CellError errorObj = new CellError(sheetName, rowIndex, templateExcelInfo.getCellNum(), proName, null, errorMsg, e);
                                        cellErrors.add(errorObj);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return obj;
    }

    /**
     * 匹配相应的字段
     *
     * @param dataClass
     * @param dataRow
     * @param cellErrors
     * @param sheetName
     * @param rowIndex
     * @param obj
     * @param colIndex
     * @param titleName
     * @param cell
     * @throws Exception
     */
    private static void matchFields(Class<?> dataClass, Row dataRow, List<CellError> cellErrors, String sheetName,
                                    int rowIndex, Object obj, int colIndex, String titleName, Cell cell) throws Exception {
        if (cell != null && !cell.toString().trim().equals("")) {
            //获取类中所有的字段
            Field[] fields = dataClass.getDeclaredFields();
            int fieldCount = 0;
            for (Field field : fields) {
                fieldCount++;
                //获取标记了ExcelAnno的注解字段
                if (field.isAnnotationPresent(ExcelAnno.class)) {
                    ExcelAnno f = field.getAnnotation(ExcelAnno.class);
                    //实体中注解的属性名称
                    String cellName = f.cellName();
                    if (cellName != null && !cellName.isEmpty()) {
                        //匹配到实体中相应的字段
                        if (chineseCompare(cellName, titleName, "UTF-8")) {
                            //打开实体中私有变量的权限
                            field.setAccessible(true);
                            //实体中变量赋值
                            try {
                                field.set(obj, getObj(field.getType(), cell));
                                break;
                            } catch (Exception e) {
                                String errorMsg = "第[" + dataRow.getRowNum() + "]行，字段:[" + cellName + "]的数据类型不正确";
                                CellError errorObj = new CellError(sheetName, rowIndex, colIndex, null, titleName, errorMsg, e);
                                cellErrors.add(errorObj);
                            }
                        }
                    } else {
                        logger.info(Thread.currentThread() + "实体：" + obj.getClass().getSimpleName() + "中的：" + field.getName() + " 未配置cellName属性");
                        continue;
                    }
                    if (fieldCount == fields.length) {
                        //标明没有找到匹配的属性字段
                        logger.info(Thread.currentThread() + "模板中的：" + sheetName + "[" + titleName + "]未与实体：" + obj.getClass().getSimpleName() + " 对应");
                    }
                }
            }
        }
    }


    /**
     * Excel 中将数字转化为字符 如 1 转为 A
     *
     * @param columnIndex
     * @return
     */
    private static String excelColIndexToStr(int columnIndex) {
        if (columnIndex <= 0) {
            return null;
        }
        String columnStr = "";
        columnIndex--;
        do {
            if (columnStr.length() > 0) {
                columnIndex--;
            }
            columnStr = ((char) (columnIndex % 26 + (int) 'A')) + columnStr;
            columnIndex = (int) ((columnIndex - columnIndex % 26) / 26);
        } while (columnIndex > 0);
        return columnStr;
    }

    /**
     * Excel 中将字母转化为数字 如A -> 1
     *
     * @param column
     * @return
     */
    public static Integer excelColStrToNum(String column) {
        int num = 0;
        int result = 0;
        int length = column.length();
        for (int i = 0; i < length; i++) {
            char ch = column.charAt(length - i - 1);
            num = (int) (ch - 'A' + 1);
            num *= Math.pow(26, i);
            result += num;
        }
        return result;
    }


    /**
     * 判断Excel中是否为空行
     *
     * @param dataRow
     * @return
     */
    public static boolean isBlankRow(Row dataRow) {
        if (Objects.nonNull(dataRow)) {
            for (int i = dataRow.getFirstCellNum(); i < dataRow.getLastCellNum(); i++) {
                Cell cell = dataRow.getCell(i);
                if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                    return false;
            }
            return true;
        }
        return true;
    }

    /**
     * @param clazz 实体中属性字段的类型
     * @param cell  Excel中的单元格
     * @return 返回单元格对应的实体
     * @throws Exception
     */
    public static Object getObj(Class clazz, Cell cell) throws Exception {
        if (cell == null) {
            return null;
        }
        String cellValue = getCellValue(cell);
        if ("java.util.Date".equalsIgnoreCase(clazz.getName())) {
            if (cellValue.matches("\\d{4}/\\d{1,2}/\\d{1,2}"))
                return ZWDateUtil.getUtilDate(cellValue, "yyyy/MM/dd");
            else if (cellValue.matches("\\d{4}-\\d{2}-\\d{2}"))
                return ZWDateUtil.getUtilDate(cellValue, "yyyy-MM-dd");
            else if (cellValue.matches("^\\d{4}\\d{2}\\d{2}"))
                return ZWDateUtil.getUtilDate(cellValue, "yyyyMMdd");
            else if (cellValue.matches("\\d{4}.\\d{1,2}.\\d{1,2}"))
                return ZWDateUtil.getUtilDate(cellValue, "yyyy.MM.dd");
            else
                throw new RuntimeException("日期格式不合适");
        } else if ("java.lang.Integer".equalsIgnoreCase(clazz.getName())) {
            return Integer.parseInt(cellValue);
        } else if ("java.lang.Short".equalsIgnoreCase(clazz.getName())) {
            return Short.parseShort(cellValue);
        } else if ("java.lang.Double".equalsIgnoreCase(clazz.getName())) {
            BigDecimal big = new BigDecimal(cellValue);
            return big.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else if ("java.math.BigDecimal".equalsIgnoreCase(clazz.getName())) {
            return new BigDecimal(cellValue).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            Constructor con = clazz.getConstructor(String.class);
            return con.newInstance(cellValue);
        }
    }

    /**
     * 解析每个Sheet页的头部信息
     *
     * @param startCol 开始列
     * @param titleRow 头部信息所在的行
     * @return
     */
    public static Map<Integer, String> parseExcelHeader(int startCol, Row titleRow) throws Exception {
        try {
            int lastCellNum = titleRow.getLastCellNum();
            if (startCol > lastCellNum) {
                throw new Exception("Excel数据模板开始列大于数据总列数");
            }
            Map<Integer, String> headerMap = new HashMap<>();
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

    /**
     * 中文字符串比较
     *
     * @param str1
     * @param str2
     * @param engCode 字符编码
     * @return
     * @throws Exception
     */
    private static Boolean chineseCompare(String str1, String str2, String engCode) throws Exception {
        String tmpStr1 = new String(str1.getBytes(engCode));
        String tmpStr2 = new String(str2.getBytes(engCode));
        if (tmpStr1.equals(tmpStr2)) {
            return true;
        }
        return false;
    }

    /**
     * 获取单元格的值
     *
     * @param cell
     * @return
     */
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        String cellValue = null;
        //根据CellTYpe动态获取Excel中的值
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                cellValue = "";
                break;
            case Cell.CELL_TYPE_ERROR:
                cellValue = "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = String.valueOf(ZWDateUtil.fomratterDate(cell.getDateCellValue(), Constants.DATE_FORMAT));
                } else {
                    DecimalFormat df = new DecimalFormat("#.#########");
                    cellValue = df.format(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_FORMULA:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = String.valueOf(ZWDateUtil.fomratterDate(cell.getDateCellValue(), Constants.DATE_FORMAT));
                } else {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case Cell.CELL_TYPE_STRING:
                cellValue = StringUtils.trim(cell.getStringCellValue());
                break;
            default:
                break;
        }
        return cellValue;
    }

    /**
     * 生成Excel
     *
     * @param dataList  数据List
     * @param titleList 模板头文件
     * @param proNames  实体属性list
     * @param startRow  开始行
     * @param startCol  开始列
     * @return
     * @throws Exception
     */
    public static void createExcel(Workbook workbook, Sheet sheet, List dataList, String[] titleList, String[] proNames, int startRow, int startCol) throws Exception {
        CellStyle headStyle = workbook.createCellStyle();
        CellStyle BodyStyle = workbook.createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
        //设置头文件字体
        Font fontTitle = workbook.createFont();
        fontTitle.setFontName("黑体");
        fontTitle.setFontHeightInPoints((short) 12);//设置字体大小
        headStyle.setFont(fontTitle);
        Font fontBody = workbook.createFont();
        fontBody.setFontName("宋体");
        fontBody.setFontHeightInPoints((short) 11);
        BodyStyle.setFont(fontBody);

        //写入头文件
        if (Objects.nonNull(titleList)) {
            Row row = sheet.createRow((short) startRow);
            Cell cell = null;
            for (int j = 0; j < titleList.length; j++) {
                cell = row.createCell((short) (startCol + j));
                cell.setCellValue(titleList[j]);
                cell.setCellStyle(headStyle);
            }
        }
        if (!dataList.isEmpty()) {
            int irowNum = startRow + 1;
            for (int i = 0; i < dataList.size(); i++) {
                Object obj = dataList.get(i);
                Row row = sheet.createRow(irowNum + i);
                for (int k = 0; k < proNames.length; k++) {
                    String exportItem = proNames[k];
                    String valueStr = null;
                    if ((getProValue(exportItem, obj)) instanceof Date) {
                        valueStr = ZWDateUtil.fomratterDate((Date) getProValue(exportItem, obj), Constants.DATE_FORMAT);
                    } else if ((getProValue(exportItem, obj)) instanceof Integer) {
                        valueStr = ((Integer) getProValue(exportItem, obj)).toString();
                    } else if ((getProValue(exportItem, obj)) instanceof Double) {
                        valueStr = ((Double) getProValue(exportItem, obj)).toString();
                    } else {
                        valueStr = (String) getProValue(exportItem, obj);
                    }
                    Cell cell = row.createCell(startCol + k, CellType.STRING);
                    cell.setCellValue(valueStr);
                    cell.setCellStyle(BodyStyle);
                }
            }
        }
    }

    public static Object getProValue(String exportItem, Object obj) {
        if (obj instanceof HashMap) {
            return ((HashMap) obj).get(exportItem);
        } else {
            String firstLetter = exportItem.substring(0, 1).toUpperCase();
            String getMethodName = "get" + firstLetter + exportItem.substring(1);
            Method getMethod = null;
            try {
                getMethod = obj.getClass().getMethod(getMethodName, new Class[]{});
                return getMethod.invoke(obj, new Object[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
