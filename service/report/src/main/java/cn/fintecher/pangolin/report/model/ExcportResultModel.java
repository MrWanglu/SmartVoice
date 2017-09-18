package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.AreaCode;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.Personal;
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
    private Department department;
    private AreaCode areaCode;

}
