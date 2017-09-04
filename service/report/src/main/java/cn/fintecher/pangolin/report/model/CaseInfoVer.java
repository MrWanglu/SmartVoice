package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:42 2017/9/4
 */

@Data
public class CaseInfoVer {
    private Integer page;
    private Integer size;
    private List<CaseInfoVerModel> content;
}