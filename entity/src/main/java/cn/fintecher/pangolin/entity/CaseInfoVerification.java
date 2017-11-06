package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationModel.java,v 0.1 2017/8/31 15:52 yuanyanting Exp $$
 */
@Entity
@Table(name = "case_info_verification")
@Data
public class CaseInfoVerification extends BaseEntity {

    @OneToOne
    @ApiModelProperty("案件Id")
    @JoinColumn(name = "case_id")
    private CaseInfo caseInfo;

    @ApiModelProperty("公司code码")
    private String companyCode;

    @ApiModelProperty("当前催收员")
    private String operator;

    @ApiModelProperty("操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;

    @ApiModelProperty("核销说明")
    private String state;

    @ApiModelProperty("打包状态")
    private Integer packingStatus;

    /**
     * @Description 打包状态类
     */
    public enum PackingStatus {
        //未挂起
        PACKED(250, "已打包"),
        //挂起
        NO_PACKED(251, "未打包");
        private Integer value;

        private String remark;

        PackingStatus(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }
}