package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author : sunyanping
 * @Description : 接收文件上传到服务器时添加的附属数据以及需要的文件ID
 * @Date : 2017/6/8.
 */
@Data
public class UploadUserBackcashPlanExcelInfo {
    @ApiModelProperty("文件ID")
    private String fileId;
    @ApiModelProperty("公司code")
    private String companyCode;
}
