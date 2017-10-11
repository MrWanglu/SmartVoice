package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.annotation.ExcelAnno;
import cn.fintecher.pangolin.dataimp.entity.DataImportRecord;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import cn.fintecher.pangolin.dataimp.model.ColumnError;
import cn.fintecher.pangolin.dataimp.repository.DataInfoExcelRepository;
import cn.fintecher.pangolin.dataimp.repository.RowErrorRepository;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.entity.util.MobileUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by sun on 2017/9/24.
 */
@Component
public class ParseExcelTask {

    private final Logger logger = LoggerFactory.getLogger(ParseExcelTask.class);

    @Autowired
    MongoSequenceService mongoSequenceService;
    @Autowired
    DataInfoExcelRepository dataInfoExcelRepository;
    @Autowired
    RowErrorRepository rowErrorRepository;

    @Async
    public void parseRow(Class<?> dataClass, Row dataRow, int startCol, Map<Integer, String> headerMap, RowError rowError,
                         DataImportRecord dataImportRecord, int rowIndex, List<TemplateExcelInfo> templateExcelInfos,
                         CopyOnWriteArrayList<Integer> dataIndex) {
        logger.debug("线程{}正在解析第{}行数据...", Thread.currentThread(), rowIndex);
        if (dataIndex.contains(rowIndex)) {
            return;
        }
        List flag = new ArrayList<>();
        List<ColumnError> columnErrorList = new ArrayList<>();

        //反射创建实体对象
        Object obj = null;
        rowError.setRowIndex(rowIndex + 1);
        try {
            obj = dataClass.newInstance();
            //默认数据模板
            if (Objects.isNull(templateExcelInfos)) {
                //循环解析每行中的每个单元格
                for (int colIndex = startCol; colIndex < dataRow.getLastCellNum(); colIndex++) {
                    //获取该列对应的头部信息中文
                    String titleName = headerMap.get(colIndex);
                    Cell cell = dataRow.getCell(colIndex);
                    matchFields(dataClass, columnErrorList, obj, colIndex, titleName, cell, flag);
                }
            } else {
                //配置模板
                for (TemplateExcelInfo templateExcelInfo : templateExcelInfos) {
                    if (StringUtils.isNotBlank(templateExcelInfo.getRelateName())) {
                        Cell cell = dataRow.getCell(templateExcelInfo.getCellNum());
                        matchFields(dataClass, columnErrorList, obj, templateExcelInfo.getCellNum(), templateExcelInfo.getCellName(), cell, flag);
                    }
                }
            }
            if (!flag.isEmpty()) {
                DataInfoExcel dataInfoExcel = (DataInfoExcel) obj;
                saveDataInfoExcelAndError(dataInfoExcel, dataImportRecord, columnErrorList, rowError, rowIndex);
            }
        } catch (Exception e) {
            logger.error("线程解析第行{}数据错误", rowIndex);
        }
        dataIndex.add(rowIndex);
        logger.debug("线程{}解析完成第{}行数据...", Thread.currentThread(), rowIndex);
        return;
    }

    private void saveDataInfoExcelAndError(DataInfoExcel dataInfoExcel, DataImportRecord dataImportRecord,
                                           List<ColumnError> columnErrorList, RowError rowError, int rowIndex) throws Exception {
        dataInfoExcel.setBatchNumber(dataImportRecord.getBatchNumber());
        dataInfoExcel.setDataSources(Constants.DataSource.IMPORT.getValue());
        dataInfoExcel.setPrinCode(dataImportRecord.getPrincipalId());
        dataInfoExcel.setPrinName(dataImportRecord.getPrincipalName());
        dataInfoExcel.setOperator(dataImportRecord.getOperator());
        dataInfoExcel.setOperatorName(dataImportRecord.getOperatorName());
        dataInfoExcel.setOperatorTime(ZWDateUtil.getNowDateTime());
        dataInfoExcel.setCompanyCode(dataImportRecord.getCompanyCode());
        dataInfoExcel.setPaymentStatus("M".concat(String.valueOf(dataInfoExcel.getOverDuePeriods() == null ? "M0" : dataInfoExcel.getOverDuePeriods())));
        dataInfoExcel.setDelegationDate(dataImportRecord.getDelegationDate());
        dataInfoExcel.setCloseDate(dataImportRecord.getCloseDate());
        String caseNumber = mongoSequenceService.getNextSeq(Constants.CASE_SEQ, dataImportRecord.getCompanyCode(), Constants.CASE_SEQ_LENGTH).concat(dataImportRecord.getCompanySequence());
        dataInfoExcel.setCaseNumber(caseNumber);
        dataInfoExcel.setRecoverWay(dataImportRecord.getRecoverWay());
        if (!columnErrorList.isEmpty()) {
            rowError.setName(dataInfoExcel.getPersonalName());
            rowError.setIdCard(dataInfoExcel.getIdCard());
            rowError.setPhone(dataInfoExcel.getMobileNo());
            rowError.setRowIndex(rowIndex + 1);
            rowError.setCaseNumber(caseNumber);
            rowError.setBatchNumber(dataImportRecord.getBatchNumber());
            StringBuilder errorSb = new StringBuilder();
            int mark = DataInfoExcel.Color.NONE.getValue();
            for (ColumnError columnError : columnErrorList) {
                errorSb.append("列【".concat(excelColIndexToStr(columnError.getColumnIndex())).concat("】"));
                errorSb.append(columnError.getTitleMsg().concat(","));
                errorSb.append(columnError.getErrorMsg());
                if (columnError.getErrorLevel() == ColumnError.ErrorLevel.FORCE.getValue()) {
                    errorSb.append("(".concat(ColumnError.ErrorLevel.FORCE.getRemark()).concat(")").concat(";"));
                    mark = DataInfoExcel.Color.RED.getValue();
                } else {
                    errorSb.append("(".concat(ColumnError.ErrorLevel.PROMPT.getRemark()).concat(")").concat(";"));
                    if (mark != DataInfoExcel.Color.RED.getValue()) {
                        mark = DataInfoExcel.Color.YELLOW.getValue();
                    }
                }
            }
            rowError.setErrorMsg(errorSb.toString());
            rowError.setCompanyCode(dataImportRecord.getCompanyCode());
            rowError.setCaseAmount(dataInfoExcel.getOverdueAmount());
            dataInfoExcel.setColor(mark);
            rowErrorRepository.save(rowError);
        }
        dataInfoExcelRepository.save(dataInfoExcel);
    }

    /**
     * 列转换为字母
     * @param columnIndex
     * @return
     */
    public String excelColIndexToStr(int columnIndex) {
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

    private void matchFields(Class<?> dataClass, List<ColumnError> columnErrorList, Object obj, int colIndex, String titleName, Cell cell, List flag) throws Exception {
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
                        ColumnError columnError = new ColumnError();
                        columnError.setColumnIndex(colIndex + 1);
                        columnError.setTitleMsg(cellName);
                        //打开实体中私有变量的权限
                        field.setAccessible(true);
                        //实体中变量赋值
                        try {
                            // 获取数据或者错误信息
                            Map<Object, ColumnError> map = validityDataGetFieldValue(field, cell, columnError, flag);
                            Map.Entry<Object, ColumnError> next = map.entrySet().iterator().next();
                            field.set(obj, next.getKey());
                            if (StringUtils.isNotBlank(next.getValue().getErrorMsg())) {
                                ColumnError value = next.getValue();
                                columnErrorList.add(value);
                            }
                            break;
                        } catch (Exception e) {
                            logger.debug(e.getMessage());
                            throw new RuntimeException(e.getMessage());
                        }
                    }
                } else {
                    logger.info(Thread.currentThread() + "实体：" + obj.getClass().getSimpleName() + "中的：" + field.getName() + " 未配置cellName属性");
                    continue;
                }
                if (fieldCount == fields.length) {
                    //标明没有找到匹配的属性字段
                    logger.info(Thread.currentThread() + "模板中的：[" + titleName + "]未与实体：" + obj.getClass().getSimpleName() + " 对应");
                }
            }
        }
    }

    private Map<Object, ColumnError> validityDataGetFieldValue(Field field, Cell cell, ColumnError columnError, List flag) throws ParseException {
        Map<Object, ColumnError> map = new HashedMap(1);
        String cellValue = getCellValue(cell);
        cellValue = filterEmoji(cellValue, "");
        if (cellValue != null || !cellValue.equals("")) {
            flag.add(true);
        }
        ExcelAnno.FieldCheck fieldCheck = field.getAnnotation(ExcelAnno.class).fieldCheck();
        switch (fieldCheck) {
            case PERSONAL_NAME:
                if (StringUtils.equalsIgnoreCase(cellValue, "")) {
                    columnError.setErrorMsg("客户姓名为空");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(cellValue, columnError);
                    break;
                }
                map.put(cellValue, columnError);
                break;
            case IDCARD:
                if (StringUtils.equalsIgnoreCase(cellValue, "")) {
                    columnError.setErrorMsg("身份证号为空");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(cellValue, columnError);
                    break;
                }
                if (!IdcardUtils.validateCard(cellValue)) {
                    columnError.setErrorMsg("身份证号无效");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(cellValue, columnError);
                    break;
                }
                map.put(cellValue, columnError);
                break;
            case PRODUCT_NAME:
                if (StringUtils.equalsIgnoreCase(cellValue, "")) {
                    columnError.setErrorMsg("产品名称为空");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(cellValue, columnError);
                    break;
                }
                map.put(cellValue, columnError);
                break;
            case CASE_AMOUNT:
                if (StringUtils.equalsIgnoreCase(cellValue, "")) {
                    columnError.setErrorMsg("案件金额为空");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(0.00, columnError);
                    break;
                }
                Double dou = 0D;
                try {
                    dou = Double.parseDouble(cellValue);
                } catch (NumberFormatException e) {
                    columnError.setErrorMsg("数据类型错误");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(dou, columnError);
                    break;
                }
                map.put(dou, columnError);
                break;
            case PHONE_NUMBER:
                if (cellValue.length() >= 16) {
                    columnError.setErrorMsg("电话号码长度过长");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                    map.put(cellValue, columnError);
                    break;
                } else if (cellValue != "" && !MobileUtil.checkMobile(cellValue) && !MobileUtil.checkPhone(cellValue)) {
                    columnError.setErrorMsg("电话号码不合规");
                    columnError.setErrorLevel(ColumnError.ErrorLevel.PROMPT.getValue());
                    map.put(cellValue, columnError);
                    break;
                } else {
                    map.put(cellValue, columnError);
                    break;
                }
            case NONE:
                ExcelAnno.FieldType fieldType = field.getAnnotation(ExcelAnno.class).fieldType();
                switch (fieldType) {
                    case STRING:
                        map.put(cellValue, columnError);
                        break;
                    case INTEGER:
                        Integer inte = 0;
                        try {
                            if (cellValue != null && !cellValue.equals("")) {
                                inte = Integer.parseInt(cellValue);
                            }
                        } catch (NumberFormatException e) {
                            columnError.setErrorMsg("数据类型错误");
                            columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                            map.put(inte, columnError);
                            break;
                        }
                        map.put(inte, columnError);
                        break;
                    case DOUBLE:
                        Double dou1 = 0D;
                        try {
                            if (cellValue != null && !cellValue.equals("")) {
                                dou1 = Double.parseDouble(cellValue);
                            }
                        } catch (NumberFormatException e) {
                            columnError.setErrorMsg("数据类型错误");
                            columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                            map.put(dou1, columnError);
                            break;
                        }
                        map.put(dou1, columnError);
                        break;
                    case DATE:
                        if (cellValue.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                            map.put(ZWDateUtil.getUtilDate(cellValue, "yyyy/MM/dd"), columnError);
                            break;
                        } else if (cellValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            map.put(ZWDateUtil.getUtilDate(cellValue, "yyyy-MM-dd"), columnError);
                            break;
                        } else if (cellValue.matches("^\\d{4}\\d{2}\\d{2}")) {
                            map.put(ZWDateUtil.getUtilDate(cellValue, "yyyyMMdd"), columnError);
                            break;
                        } else if (cellValue.matches("\\d{4}.\\d{1,2}.\\d{1,2}")) {
                            map.put(ZWDateUtil.getUtilDate(cellValue, "yyyy.MM.dd"), columnError);
                            break;
                        } else if (cellValue == null || cellValue.equals("")) {
                            map.put(null, columnError);
                            break;
                        } else {
                            columnError.setErrorMsg("日期格式错误");
                            columnError.setErrorLevel(ColumnError.ErrorLevel.FORCE.getValue());
                            map.put(ZWDateUtil.getUtilDate("1970-01-01", "yyyy-MM-dd"), columnError);
                            break;
                        }
                }
        }
        return map;
    }

    private String filterEmoji(String source, String slipStr) {
        if (StringUtils.isNotBlank(source)) {
            return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", slipStr);
        } else {
            return source;
        }
    }

    private Boolean chineseCompare(String str1, String str2, String engCode) throws Exception {
        String tmpStr1 = new String(str1.getBytes(engCode));
        String tmpStr2 = new String(str2.getBytes(engCode));
        if (tmpStr1.equals(tmpStr2)) {
            return true;
        }
        return false;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        String cellValue = "";
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

    public Object getObj(Class clazz, Cell cell) throws Exception {
        if (cell == null) {
            return null;
        }
        String cellValue = getCellValue(cell);
        Constructor con = clazz.getConstructor(String.class);
        return con.newInstance(cellValue);
    }
}
