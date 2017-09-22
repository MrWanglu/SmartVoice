package cn.fintecher.pangolin.dataimp.entity;

import cn.fintecher.pangolin.dataimp.model.ColumnError;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by sunyanping on 2017/9/22.
 */
@Data
@Document
@ApiModel(description = "数据行错误信息")
public class RowError {
    @ApiModelProperty("唯一标识（主键）")
    @Id
    private String Id;
    @ApiModelProperty("sheet名称")
    private String sheetName;
    @ApiModelProperty("行数")
    private Integer rowIndex;
    @ApiModelProperty("错误信息")
    private List<ColumnError> columnErrorList;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("案件编号")
    private String caseNumber;
}
