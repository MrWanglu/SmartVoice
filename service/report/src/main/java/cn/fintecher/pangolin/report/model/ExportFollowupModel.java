package cn.fintecher.pangolin.report.model;

import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerModel.java,v 0.1 2017/9/1 13:56 yuanyanting Exp $$
 */

@Data
@Entity
public class ExportFollowupModel {

    private String companyCode; // 公司code

    private List<String> list; // 案件编号集合
}
