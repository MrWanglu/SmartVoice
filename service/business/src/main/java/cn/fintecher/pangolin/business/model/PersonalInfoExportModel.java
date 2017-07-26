package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/24.
 */
@Data
public class PersonalInfoExportModel {
    @ApiModelProperty("导出维度")
    private Integer exportType;
    @ApiModelProperty("数据过滤")
    private Set<Object> dataFilter;
    @ApiModelProperty("数据配置项")
    private Map<String, List<String>> dataInfo;
}
