package cn.fintecher.pangolin.business.utils;

import org.apache.poi.ss.usermodel.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Author : sunyanping
 * @Description : 导出Excel工具
 * @Date : 2017/7/24.
 */
public class ExcelExportHelper {

    public static void createExcel(Workbook workbook, Sheet sheet, Map<String, String> headMap, List<Map<String, Object>> dataList, int startRow, int startCol) throws Exception {

        CellStyle headStyle = workbook.createCellStyle();
        CellStyle bodyStyle = workbook.createCellStyle();
        headStyle.setAlignment(HorizontalAlignment.CENTER); // 居中
        //设置头文件字体
        Font fontTitle = workbook.createFont();
        fontTitle.setFontName("黑体");
        fontTitle.setFontHeightInPoints((short) 12);//设置字体大小
        headStyle.setFont(fontTitle);
        Font fontBody = workbook.createFont();
        fontBody.setFontName("宋体");
        fontBody.setFontHeightInPoints((short) 11);
        bodyStyle.setFont(fontBody);

        // 写入表头
        if (!headMap.isEmpty()) {
            // 创建行
            Row row = sheet.createRow(startRow);
            for (Iterator<String> iter = headMap.keySet().iterator(); iter.hasNext(); ) {
                String fieldName = iter.next();
                // 创建单元格并写入值
                Cell cell = row.createCell(startCol++);
                cell.setCellValue(fieldName);
                cell.setCellStyle(headStyle);
            }
        }
        startRow++;

        // 写入数据
        if (!dataList.isEmpty()) {
            for (Map<String, Object> dataMap : dataList) {
                Row dataRow = sheet.createRow(startRow);
                Iterator<Map.Entry<String, String>> iterator = headMap.entrySet().iterator();
                int i = -1;
                while (iterator.hasNext()) {
                    i++;
                    Cell cell = dataRow.createCell(i);
                    for (int j = 0; j < dataMap.size(); j++) {
                        String key = iterator.next().getKey();
                        Object o = dataMap.get(key);
                        if (o == null) {
                            cell.setCellValue("");
                            cell.setCellStyle(bodyStyle);
                        } else {
                            cell.setCellValue(o.toString());
                            cell.setCellStyle(bodyStyle);
                        }
                    }
                }
            }
        }
    }

//    public static void main(String[] args) throws Exception {
//        HSSFWorkbook workbook = new HSSFWorkbook();
//        HSSFSheet sheet = workbook.createSheet("sheet1");
//        Map<String, String> map = new HashMap<>();
//        map.put("name", "姓名");
//        map.put("age", "年龄");
//        List<Map<String,Object>> list = new ArrayList<>();
//        Map<String,Object> dataMap = new HashMap<>();
//        dataMap.put("name","王华");
//        dataMap.put("age","24");
//        Map<String,Object> dataMap1 = new HashMap<>();
//        dataMap1.put("name","王华");
//        dataMap1.put("age","24");
//        list.add(dataMap);
//        list.add(dataMap1);
//        createExcel(workbook, sheet, map,list,0,0);
//    }
}
