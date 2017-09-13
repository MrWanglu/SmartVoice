package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.annotation.ExcelAnno;
import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import cn.fintecher.pangolin.dataimp.entity.TemplateExcelInfo;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luqiang on 2017/7/25.
 */
@Service(value="templateDataModelService")
public class TemplateDataModelService {
    private final Logger logger = LoggerFactory.getLogger(TemplateDataModelService.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RestTemplate restTemplate;

    public List<TemplateExcelInfo> importExcelData(String filePath, String rowNum, String colNum) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("线程 {} 开始解析Excel..............................................", Thread.currentThread() + "开始时间" + startTime);
        int rowIndex = 0;
        int colIndex = 0;
        int rowMax = 0;
        Optional<String> rowOpt = Optional.of(rowNum);
        if (rowOpt.isPresent()) {
            try {
                rowIndex = Integer.parseInt(rowNum) - 1;
                rowMax = rowIndex + 5;//预读5行
            } catch (Exception e) {
                throw new NumberFormatException("行号太大");
            }

        }
        Optional<String> colOpt = Optional.of(colNum);
        if (colOpt.isPresent()) {
            try {
                colIndex = Integer.parseInt(excelColStrToNum(colNum)) - 1;
            } catch (Exception e) {
                throw new NumberFormatException("列号太大");
            }

        }

        List<TemplateExcelInfo> list = new ArrayList();
        TemplateExcelInfo excelTemplate = null;
        String fileType = filePath.substring(filePath.lastIndexOf('.') + 1);
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(filePath,
                HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);
        byte[] result = response.getBody();
        Map<String, String> mapType = getFieldType();
        Map<String, String> mapTypeName = getFieldTypeName();
        InputStream inputStream = new ByteArrayInputStream(result);
        if ("xls".equals(fileType)) {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
            HSSFRow hssfRow = hssfSheet.getRow(rowIndex);
            if (rowIndex > hssfSheet.getLastRowNum()) {
                throw new IndexOutOfBoundsException("最大" + hssfSheet.getLastRowNum() + "行");
            }
            if (hssfRow != null) {
                for (; rowIndex <= rowMax; rowIndex++) {
                    hssfRow = hssfSheet.getRow(rowIndex);

                    try {
                        int length = hssfRow.getLastCellNum();
                        for (; colIndex < hssfRow.getLastCellNum(); colIndex++) {
                            if (Integer.parseInt(excelColStrToNum(colNum)) > hssfRow.getLastCellNum()) {
                                throw new IndexOutOfBoundsException("最大" + hssfRow.getLastCellNum() + "列");
                            }
                            HSSFCell cell = hssfRow.getCell(colIndex);
                            excelTemplate = new TemplateExcelInfo();
                            excelTemplate.setCellName(getCellValue(cell));
                            excelTemplate.setCellNum(colIndex);
                            excelTemplate.setColNum(excelColIndexToStr(colIndex + 1));

                            if (cell.getCellType() == 0) {
                                excelTemplate.setCellTypeName((mapTypeName.containsKey(cell.getNumericCellValue()) ? mapTypeName.get(cell.getNumericCellValue()) : null));
                                excelTemplate.setCellType(mapType.containsKey(cell.getNumericCellValue()) ? mapType.get(cell.getNumericCellValue()) : null);
                            } else if (cell.getCellType() == 1) {
                                excelTemplate.setCellType(mapType.containsKey(cell.getStringCellValue()) ? mapType.get(cell.getStringCellValue()) : null);
                                excelTemplate.setCellTypeName(mapTypeName.containsKey(cell.getStringCellValue()) ? mapTypeName.get(cell.getStringCellValue()) : null);
                            }
                            list.add(excelTemplate);

                        }
                    } catch (NullPointerException e) {
                    }
                }
            }
        } else if ("xlsx".equals(fileType)) {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
            XSSFRow xssfRow = xssfSheet.getRow(rowIndex);
            if (rowIndex > xssfSheet.getLastRowNum()) {
                throw new IndexOutOfBoundsException("最大" + xssfSheet.getLastRowNum() + "行");
            }
            if (xssfRow != null) {
                for (; rowIndex <= rowMax; rowIndex++) {
                    xssfRow = xssfSheet.getRow(rowIndex);
                    try {
                        for (; colIndex < xssfRow.getLastCellNum(); colIndex++) {

                            if (Integer.parseInt(excelColStrToNum(colNum)) > xssfRow.getLastCellNum()) {
                                throw new IndexOutOfBoundsException("最大" + xssfRow.getLastCellNum() + "列");
                            }
                            XSSFCell cell = xssfRow.getCell(colIndex);
                            excelTemplate = new TemplateExcelInfo();
                            excelTemplate.setCellName(getCellValue(cell));
                            if (cell.getCellType() == 0) {
                                excelTemplate.setCellType(mapType.containsKey(cell.getNumericCellValue()) ? mapType.get(cell.getNumericCellValue()) : null);
                                excelTemplate.setCellTypeName(mapTypeName.containsKey(cell.getNumericCellValue()) ? mapTypeName.get(cell.getNumericCellValue()) : null);
                            } else if (cell.getCellType() == 1) {
                                excelTemplate.setCellType(mapType.containsKey(cell.getStringCellValue()) ? mapType.get(cell.getStringCellValue()) : null);
                                excelTemplate.setCellTypeName(mapTypeName.containsKey(cell.getStringCellValue()) ? mapTypeName.get(cell.getStringCellValue()) : null);
                            }
                            excelTemplate.setCellNum(colIndex);
                            excelTemplate.setColNum(excelColIndexToStr(colIndex + 1));
                            list.add(excelTemplate);
                        }
                    } catch (NullPointerException e) {
                    }
                }
            }
        } else {
            throw new Exception("不是Excel文件不能解析");
        }
        return list;
    }
    public String excelColStrToNum(String column) {
        int num = 0;
        int result = 0;
        int length = column.length();
        for (int i = 0; i < length; i++) {
            char ch = column.charAt(length - i - 1);
            num = (int) (ch - 'A' + 1);
            num *= Math.pow(26, i);
            result += num;
        }
        return String.valueOf(result);
    }
    private Map<String, String> getFieldTypeName() throws Exception {
        Map<String, String> mapTypeName = new ConcurrentHashMap();
        DataInfoExcel bean = new DataInfoExcel();
        Class dataInfo = (Class) bean.getClass();
        Field[] fs = dataInfo.getDeclaredFields();
        for (Field field1 : fs) {
            ExcelAnno f = field1.getAnnotation(ExcelAnno.class);
            if (Objects.nonNull(f)) {
                mapTypeName.put(f.cellName(), getTypeName(field1.getType().getName()));
            }
        }
        return mapTypeName;
    }

    private Map<String, String> getFieldType() throws Exception {
        Map<String, String> mapType = new ConcurrentHashMap();
        DataInfoExcel bean = new DataInfoExcel();
        Class dataInfo = (Class) bean.getClass();
        Field[] fs = dataInfo.getDeclaredFields();
        for (Field field1 : fs) {
            ExcelAnno f = field1.getAnnotation(ExcelAnno.class);
            if (Objects.nonNull(f)) {
                mapType.put(f.cellName(), field1.getType().getName());
            }
        }
        return mapType;
    }

    private String getTypeName(String type) throws Exception {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "java.lang.String":
            case "class java.lang.String":
                return "字符串";
            case "java.util.Date":
            case "class java.util.Date":
                return "日期";
            case "java.lang.Integer":
            case "class java.lang.Integer":
                return "整数";
            case "java.lang.Short":
            case "class java.lang.Short":
                return "整数";
            case "java.lang.Double":
            case "class java.lang.Double":
                return "小数";
            case "java.math.BigDecimal":
            case "class java.math.BigDecimal":
                return "金额";
            default:
                return "";
        }
    }
    public String getCellValue(Cell cell) {//获取单元格内容

        if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
            return cell.getStringCellValue();
        }
        if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {    //判断是日期类型
                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
                Date dt = HSSFDateUtil.getJavaDate(cell.getNumericCellValue());//获取成DATE类型
                return dateformat.format(dt);
            } else {
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
            }
        }
        if (Cell.CELL_TYPE_BOOLEAN == cell.getCellType()) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }
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
    public List<TemplateExcelInfo> getExcelList() throws Exception {
        List<TemplateExcelInfo> list = new ArrayList<>();
        DataInfoExcel bean = new DataInfoExcel();
        Class dataInfo = (Class) bean.getClass();
        Field[] fs = dataInfo.getDeclaredFields();

        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            f.setAccessible(true);
            if (f.isAnnotationPresent(ExcelAnno.class)) {
                ExcelAnno name = f.getAnnotation(ExcelAnno.class);
                TemplateExcelInfo templateModel = new TemplateExcelInfo();
                //实体中注解的属性名称
                templateModel.setCellName(name.cellName());
                if(f.getName().equals("personalName") ||f.getName().equals("idCard")
                        || f.getName().equals("productName") || f.getName().equals("province") || f.getName().equals("city") ){
                    templateModel.setFlag(0);//0：必输
                }else{
                    templateModel.setFlag(1);//1:不是必输
                }
                templateModel.setCellCode(f.getName());
                templateModel.setCellType(f.getType().toString());
                templateModel.setCellTypeName(getTypeName(f.getType().toString()));
                list.add(templateModel);
            }

        }
        return list;
    }
    public List<String[]> processData( Map<String, String[]> map) {
        List<String[]> list = new ArrayList<>();
        list.add(map.get("custInfo"));
        list.add(map.get("custOriginAddressInfo"));
        list.add(map.get("custCarInfo"));
        list.add(map.get("custJobInfo"));
        list.add(map.get("custBalanceInfo"));
        list.add(map.get("orderInfo"));
        list.add(map.get("accountInfo"));
        //Integer custAssetNum = Integer.valueOf(map.get("custAssetNum")[0]);
        //客户联系人
        Integer custRelationNum;
        if(map.get("custRelationNum").length>0){
            custRelationNum=Integer.valueOf(map.get("custRelationNum")[0]);
        }else {
            custRelationNum = 0;
        }
        String[] custRelationInfo = map.get("custRelationInfo");
        if(custRelationNum>0){
            for(int i=1;i<=custRelationNum;i++){
                String[] custRelationTemp = new String[custRelationInfo.length];
                System.arraycopy(custRelationInfo,0,custRelationTemp,0,custRelationInfo.length);
                for(int a=0;a<custRelationInfo.length;a++){
                    custRelationTemp[a]="联系人"+i+custRelationTemp[a];
                }
                list.add(custRelationTemp);
            }
        }
        //客户房产
        Integer custAssetNum ;
        if(map.get("custAssetNum").length>0){
            custAssetNum=Integer.valueOf(map.get("custAssetNum")[0]);
        }else {
            custAssetNum = 0;
        }
        String[] custAssetInfo = map.get("custAssetInfo");
        if(custAssetNum>0){
            for(int i=1;i<=custAssetNum;i++){
                String[] custAssetTemp = new String[custAssetInfo.length];
                System.arraycopy(custAssetInfo,0,custAssetTemp,0,custAssetInfo.length);
                for(int a=0;a<custAssetInfo.length;a++){
                    custAssetTemp[a]="房产"+i+custAssetTemp[a];
                }
                list.add(custAssetTemp);
            }
        }
        return list;
    }
    public String[] CopyTheArray(String[] originalArray, List<String[]> list) {
        if(!list.isEmpty()){
            for(String[] array : list){
                if(Objects.nonNull(array)){
                    originalArray= Arrays.copyOf(originalArray,originalArray.length+array.length);
                    System.arraycopy(array,0,originalArray,originalArray.length-array.length,array.length);
                }
            }
        }
        return originalArray;
    }
}
