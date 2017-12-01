package cn.fintecher.pangolin.common.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @Author: lijian
 * @Description:
 * @Date: 2017/5/8
 */
@Document
@Data
@ApiModel(value = "appVersion", description = "app版本控制")
public class AppVersion {
    @Id
    private String id;
    @NotEmpty
    @ApiModelProperty(notes = "操作系统")
    private String os;
    @NotEmpty
    @ApiModelProperty(notes = "版本号")
    private String mobileVersion;
    @NotNull
    @ApiModelProperty(notes = "是否静默安装,0否,1是")
    private Integer silentInstall;
    @NotEmpty
    @ApiModelProperty(notes = "更新文件地址")
    private String url;
    @ApiModelProperty(notes = "发布时间")
    private Date publishTime;
    @ApiModelProperty(notes = "创建时间")
    private Date createTime;
    @ApiModelProperty(notes = "系统版本")
    private String sysVersion;
    @ApiModelProperty(notes = "创建者userName")
    private String creator;
    @ApiModelProperty(notes = "更新说明")
    private String updateLog;
    @ApiModelProperty(notes = "文件大小,字节B")
    private Integer size;
    @ApiModelProperty(notes = "更新类型,0-整包，1-应用内")
    private Integer type;
    private String companyCode;

    public enum IsSilentInstall {
        NO(1), YES(0);
        private Integer value;

        IsSilentInstall(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public enum OS {
        ANDROID("Android"), IOS("iPhone OS");
        private String value;

        OS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum UpdateType {
        FULL(0), INCREMENTAL(1);
        private Integer value;

        UpdateType(Integer value) {
            this.value = value;
        }
    }
}
