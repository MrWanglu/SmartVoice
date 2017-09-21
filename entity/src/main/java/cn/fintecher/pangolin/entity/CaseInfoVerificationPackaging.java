package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationPackaging.java,v 0.1 2017/9/21 9:31 yuanyanting Exp $$
 * 核销案件打包的实体类
 */
@Entity
@Table(name = "case_info_verification_packaging")
@Data
public class CaseInfoVerificationPackaging extends BaseEntity {

    @ApiModelProperty(notes = "案件打包的id")
    private String id;

    @ApiModelProperty(notes = "打包时间")
    private Date packagingTime;

    @ApiModelProperty(notes = "打包说明")
    private String packagingState;

    @ApiModelProperty(notes = "打包个数")
    private Integer packagingCount;

    @ApiModelProperty(notes = "总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(notes = "下载个数")
    private Integer downloadCount;

    @ApiModelProperty(notes = "下载地址")
    private String downloadAddress;

    @ApiModelProperty(notes = "操作人")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

}
