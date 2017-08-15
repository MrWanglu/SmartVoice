package cn.fintecher.pangolin.business.utils;

import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.poi.ss.usermodel.*;

import java.util.*;

/**
 * @Author : sunyanping
 * @Description : 导出Excel工具
 * @Date : 2017/7/24.
 */
public class ExcelExportHelper {

    /**
     * 导出Excel工具
     *
     * @param workbook workbook
     * @param sheet    sheet
     * @param headMap  表头（map的key为对应的字段，value为对应的名字）
     * @param dataList 数据集合（每条数据以map形式存放，key为字段名，value为对应的数据）
     * @param startRow sheet页的开始
     * @param startCol
     * @throws Exception
     */
    public static void createExcel(Workbook workbook, Sheet sheet, Map<String, String> headMap, List<Map<String, Object>> dataList, int startRow, int startCol) throws Exception {

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

        // 写入表头
        int headStartCol = startCol;
        if (!headMap.isEmpty()) {
            // 创建行
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

        // 写入数据
        if (!dataList.isEmpty()) {
            Iterator<Map.Entry<String, String>> entries = headMap.entrySet().iterator();
            // 获取到headMap中所有的key
            List<String> headKey = new ArrayList<>();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                headKey.add(entry.getKey());
            }
            int k = 1; //序号从1开始
            for (Map<String, Object> dataMap : dataList) {
                Row dataRow = sheet.createRow(++startRow);
                int dataStartCol = startCol;
                Cell indexCell = dataRow.createCell(dataStartCol++);
                indexCell.setCellValue(k++);
                indexCell.setCellStyle(bodyStyle);
                Iterator<Map.Entry<String, Object>> iterator = dataMap.entrySet().iterator();
                List<String> dataKey = new ArrayList<>(); // 数据所有的key
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> next = iterator.next();
                    dataKey.add(next.getKey());
                }
                // 遍历head
                for (String hk : headKey) {
                    Cell cell = dataRow.createCell(dataStartCol++);
                    // 遍历dataMap的key
                    for (String dk : dataKey) {
                        if (Objects.equals(dk, hk)) {
                            if (Objects.equals(dataMap.get(dk), "") || Objects.isNull(dataMap.get(dk))) {
                                cell.setCellValue("");
                                cell.setCellStyle(bodyStyle);
                            } else {
                                Object value = dataMap.get(dk);
                                if (value instanceof Date) {
                                    cell.setCellValue(ZWDateUtil.fomratterDate((Date) value, "yyyy-MM-dd"));
                                } else {
                                    cell.setCellValue(String.valueOf(value));
                                }
                                cell.setCellStyle(bodyStyle);
                            }
                            break;
                        }
                    }
                }
            }
        }
        for (int i=0;i<headMap.size();i++) {
            sheet.autoSizeColumn((short)i);
        }
    }
}
