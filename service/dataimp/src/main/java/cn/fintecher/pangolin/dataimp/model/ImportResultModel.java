package cn.fintecher.pangolin.dataimp.model;

import cn.fintecher.pangolin.dataimp.entity.RowError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/10/18.
 */
@Data
@ApiModel("导入案件后返回数据模型")
public class ImportResultModel {
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("行错误信息")
    private List<RowError> rowErrorList;
}
