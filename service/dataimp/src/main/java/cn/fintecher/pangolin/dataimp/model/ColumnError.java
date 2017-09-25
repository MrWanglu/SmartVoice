package cn.fintecher.pangolin.dataimp.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 行错误信息实体
 * Created by sunyanping on 2017/9/22.
 */
@Data
@ApiModel(description = "列错误信息")
public class ColumnError {
    @ApiModelProperty("列号")
    private Integer columnIndex;
    @ApiModelProperty("表头信息")
    private String titleMsg;
    @ApiModelProperty("错误提示信息")
    private String errorMsg;
    @ApiModelProperty("错误等级")
    private Integer errorLevel;

    public enum ErrorLevel{
        FORCE(0,"严重错误"),PROMPT(1,"提醒错误");

        private Integer value;
        private String remark;

        ErrorLevel(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }
}
