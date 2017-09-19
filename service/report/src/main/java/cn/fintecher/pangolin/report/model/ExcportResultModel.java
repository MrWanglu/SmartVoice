package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.*;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/9/18.
 */
@Data
public class ExcportResultModel {
    private String id;
    private String batchNumber;
    private String caseNumber;
    private Personal personalInfo;
    private List<CaseFollowupRecord> caseFollowupRecords;
    private AreaCode areaCode;
    private Principal principal;

}
