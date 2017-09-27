package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/27.
 */
@Data
public class VerificationApplyModel {
    private List<String> ids;//回收案件id
    private String reason;//核销申请理由
}
