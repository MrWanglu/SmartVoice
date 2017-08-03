package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.Date;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/31.
 */
@Data
public class SysNotice {

    private String id;

    private String title;

    private String content;

    private Date createTime;

    private String creator;
}
