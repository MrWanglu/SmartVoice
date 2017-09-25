package cn.fintecher.pangolin.dataimp.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description: ExcelSheet页数据对象
 * @Date 11:22 2017/3/6
 */
@Data
public class ExcelSheetObj  implements Serializable{
    private String sheetName;
    private List dataList;
    private String importMsg;
    private List<RowError> sheetErrorList;
}
