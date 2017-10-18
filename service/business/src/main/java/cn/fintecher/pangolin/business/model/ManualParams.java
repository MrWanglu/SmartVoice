package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * Created by sunyanping on 2017/9/26.
 */
@Data
@ApiModel(description = "手动分案参数")
public class ManualParams {
    @ApiModelProperty("案件编号集合")
    private List<String> caseNumberList;
    @ApiModelProperty("分配类型：0-分配到内催，1-分配到委外")
    private Integer type;
    @ApiModelProperty("选择案件到期时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closeDate;
}
