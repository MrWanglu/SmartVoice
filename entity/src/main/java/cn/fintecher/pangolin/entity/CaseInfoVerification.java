package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationModel.java,v 0.1 2017/8/31 15:52 yuanyanting Exp $$
 */
@Entity
@Table(name = "case_info_verification")
@Data
public class CaseInfoVerification extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "case_id")
    private CaseInfo caseInfo;


    private String companyCode;

}