package cn.fintecher.pangolin.dataimp.model;

import cn.fintecher.pangolin.dataimp.entity.DataInfoExcel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/8/12.
 */
@Data
public class AreaInfoModel implements Serializable {
    private List<DataInfoExcel> dataInfoList;
    private Integer total;
}
