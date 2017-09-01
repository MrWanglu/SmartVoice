package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by huyanmin on 2017/8/31.
 * @Description : 委外案件管理->回款/回退/修复
 */

@Entity
@Table(name = "out_operate_record")
@Data
public class OutBackSource extends BaseEntity {

    @ApiModelProperty("委外方ID")
    private String outId;

    @ApiModelProperty("委外案件ID")
    private String outcaseId;

    @ApiModelProperty("委外案件ID")
    private String operator;

    @ApiModelProperty("操作时间")
    private Date operateTime;

    @ApiModelProperty("特定公司的标识")
    private String companyCode;

    @ApiModelProperty("回款金额")
    private BigDecimal backAmt;

    @ApiModelProperty("操作类型")
    private String operationType;


    /**
     * @Description 操作类型
     */
    public enum operationType{
        //回款
        OUTBACKAMT(204),
        //回退
        OUTBACK(205),
        //修复
        OUTREPAIR(206);

        private Integer code;

        operationType(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }
    }


}
