package cn.fintecher.pangolin.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @Author: lijian
 * @Description:
 * @Date: 2017/5/9
 */
@Data
public class AppVersionSaveCondition {
    @NotEmpty
    @ApiModelProperty(notes = "操作系统")
    private List<String> os;

    @NotEmpty
    @ApiModelProperty(notes = "app版本号")
    private String mobileVersion;

    @NotNull
    @ApiModelProperty(notes = "是否静默安装,0否,1是")
    private Integer silentInstall;

    @NotEmpty
    @NotBlank
    @ApiModelProperty(notes = "更新文件地址")
    private String url;

    @ApiModelProperty(notes = "发布时间")
    private Date publishTime;

    @ApiModelProperty(notes = "创建时间")
    private Date createTime;

    @ApiModelProperty(notes = "系统版本")
    private String sysVersion;

    @ApiModelProperty(notes = "创建者userId")
    private String creator;

    @ApiModelProperty(notes = "更新说明")
    private String updateLog;

    @ApiModelProperty(notes = "文件大小,字节B")
    private Integer size;

    @NotNull
    @ApiModelProperty(notes = "更新类型,0-整包，1-应用内")
    private Integer type;
}
