package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "department")
@Data
@ApiModel(value = "department", description = "组织机构信息管理")
public class Department extends BaseEntity {
    @ApiModelProperty(notes = "所属公司的特定标识")
    private String companyCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pid")
    @ApiModelProperty(notes = "父机构的id")
    private Department parent;

    @ApiModelProperty(notes = "机构的名称")
    private String name;

    @ApiModelProperty(notes = "机构类型")
    private Integer type;

    @ApiModelProperty(notes = "机构编号")
    private String code;

    @ApiModelProperty(notes = "机构等级")
    private Integer level;

    @ApiModelProperty(notes = "机构状态（0是启用  1 是停用）")
    private Integer status;

    @ApiModelProperty(notes = "机构的描述")
    private String remark;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建时间")
    private Date operateTime;

    @ApiModelProperty(notes = "备用字段")
    private String field;

    /**
     * @Description 部门类型
     */
    public enum Type {

        TELEPHONE_COLLECTION(1, "电话催收"),
        OUTBOUND_COLLECTION(2, "外访催收"),
        JUDICIAL_COLLECTION(3, "司法催收"),
        OUTSOURCING_COLLECTION(4, "委外催收"),
        INTELLIGENCE_COLLECTION(5, "智能催收"),
        REMIND_COLLECTION(6, "提醒催收"),
        REPAIR_MANAGEMENT(7, "修复管理"),
        SYNTHESIZE_MANAGEMENT(196, "综合管理");
        private Integer value;
        private String remark;

        Type(Integer value, String remark) {
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
