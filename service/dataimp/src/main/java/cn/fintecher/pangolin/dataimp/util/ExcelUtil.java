package cn.fintecher.pangolin.dataimp.util;

import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: Excel读写操作
 * @Date 11:26 2017/3/3
 */
public class ExcelUtil {
    private final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);

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
