package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/27.
 */
@Data
public class CaseInfoByBatchModel {
    List<CollectingCaseInfo> content;

    int totalPages;

    long totalElements;
}
