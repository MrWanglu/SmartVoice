package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

@Data
public class CaseUpdateParams {
    //更新案件id列表
    List<String> caseInfoIds;
    //异常案件ID
    String caseInfoExceptionId;
}
