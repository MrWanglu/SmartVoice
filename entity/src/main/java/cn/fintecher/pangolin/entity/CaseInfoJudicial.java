package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseInfoJudicial.java,v 0.1 2017/9/27 10:16 yuanyanting Exp $$
 * 司法表
 */
@Data
@Entity
@Table(name = "case_info_judicial")
public class CaseInfoJudicial extends BaseEntity{

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("案件ID")
    @OneToOne
    @JoinColumn(name = "case_id")
    private CaseInfo caseInfo;

    @ApiModelProperty("操作人用户名")
    private String operatorUserName;

    @ApiModelProperty("操作人姓名")
    private String operatorRealName;

    @ApiModelProperty("操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;

    @ApiModelProperty("公司code")
    private String companyCode;
}
