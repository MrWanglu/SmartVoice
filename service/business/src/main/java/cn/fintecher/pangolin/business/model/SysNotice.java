package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author : sunyanping
 * @Description : 首页公告model
 * @Date : 2017/7/31.
 */
@Data
public class SysNotice {
    @ApiModelProperty("公告标题")
    private String title;
    @ApiModelProperty("公告内容")
    private String content;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @ApiModelProperty("操作人")
    private String creator;
}
