package cn.fintecher.pangolin.entity.util;

import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by sunyanping on 2017/9/19.
 */
public class ExcelExportUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);

    private static final int ROW_MAX = 1048576 - 1; //Excel 07版最大行数 减去表头
    private static final int SHEET_MAX = 255;   // Excel sheet页最大个数

    public static void createExcelData(SXSSFWorkbook workbook, Map<String, String> headMap, List<?> dataList, int rowData) throws Exception {
        CellStyle headStyle = workbook.createCellStyle();
        CellStyle bodyStyle = workbook.createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
        bodyStyle.setAlignment(HorizontalAlignment.CENTER); //居中
        //设置头文件字体
        Font fontTitle = workbook.createFont();
        fontTitle.setFontName("黑体");
        fontTitle.setFontHeightInPoints((short) 12);//设置字体大小
        headStyle.setFont(fontTitle);
        Font fontBody = workbook.createFont();
        fontBody.setFontName("宋体");
        fontBody.setFontHeightInPoints((short) 11);
        bodyStyle.setFont(fontBody);

        if (rowData > ROW_MAX) {
            throw new RuntimeException("每个sheet页显示的行数超过了所允许的最大行数!");
        }

        int sheetNum = getSheetNum(dataList, rowData);

        if (sheetNum > SHEET_MAX) {
            throw new RuntimeException("要导出的sheet页数量超过所允许的最大个数，可以选择将每个sheet显示的行数调大!");
        }

        int aaa = 0;
        for (int i = 0; i < sheetNum; i++) {
            logger.info("第"+ (i+1)+"页"+sheetNum);
            SXSSFSheet sheet = workbook.createSheet("sheet" + (i + 1));
            int startRow = 0;
            int headStartCol = 0;
            if (!headMap.isEmpty()) {
                Row row = sheet.createRow(startRow);
                Cell cell = row.createCell(headStartCol++);
                cell.setCellValue("序号");
                cell.setCellStyle(headStyle);
                Iterator<Map.Entry<String, String>> iterator = headMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    Cell headCell = row.createCell(headStartCol++);
                    headCell.setCellValue(next.getValue());
                    headCell.setCellStyle(headStyle);
                }
            }


            int a = dataList.size() % rowData;
            List<?> dataListSub = new ArrayList<>();
            if (a == 0) {
                dataListSub = dataList.subList(i * rowData, (i * rowData) + rowData);
            } else {
                if (a == (dataList.size() - (i * rowData))) {
                    dataListSub = dataList.subList(i * rowData, dataList.size());
                } else {
                    dataListSub = dataList.subList(i * rowData, (i * rowData) + rowData);
                }
            }

            // 写入数据
            if (!dataListSub.isEmpty()) {
                int k = 1; //序号从1开始
                for (Object object : dataListSub) {
                    int dataStartCol = 0;
                    Row dataRow = sheet.createRow(++startRow);
                    Cell indexCell = dataRow.createCell(dataStartCol++);
                    indexCell.setCellValue(k++);
                    indexCell.setCellStyle(bodyStyle);

                    Class<?> aClass = object.getClass();
                    Iterator<Map.Entry<String, String>> iterator = headMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Cell cell = dataRow.createCell(dataStartCol++);
                        Map.Entry<String, String> next = iterator.next();
                        Field declaredField = aClass.getDeclaredField(next.getKey());
                        declaredField.setAccessible(true);
                        Object value = declaredField.get(object);
                        if (value instanceof Date) {
                            cell.setCellValue(ZWDateUtil.fomratterDate((Date) value, "yyyy-MM-dd"));
                        } else {
                            cell.setCellValue(String.valueOf(value));
                        }
                        cell.setCellStyle(bodyStyle);
                    }
                    logger.info("正在写入第"+ ++aaa +"条数据");
                }
            }
        }
    }

    private static int getSheetNum(List<?> dataList, int rowData) {
        int a = dataList.size() / rowData;
        int b = dataList.size() % rowData;
        if (b == 0) {
            return a == 0 ? 1 : a;
        } else {
            return a == 0 ? 1 : a + 1;
        }
    }

    public static HashMap<String, String> createHeadMap(String[] title, Class<?> tClass) {
        HashMap<String, String> headMap = new LinkedHashMap<>();
        Field[] declaredFields = tClass.getDeclaredFields();
        for (int i = 0; i < title.length; i++) {
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(ExcelAnno.class)) {
                    ExcelAnno f = field.getAnnotation(ExcelAnno.class);
                    String cellName = f.cellName().trim();
                    String titleName = title[i].trim();
                    if (cellName.equals(titleName)) {
                        headMap.put(field.getName(), f.cellName().trim());
                        break;
                    }
                }
            }
        }
        return headMap;
    }
}
