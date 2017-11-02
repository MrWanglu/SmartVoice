package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by Administrator on 2017/9/26.
 */
@Entity
@Table(name = "export_items")
@Data
@ApiModel(value = "department", description = "组织机构信息管理")
public class ExportItem extends BaseEntity{
    private String companyCode;
    private Integer category;
    private Integer type;
    private String name;
    private Integer statu;

    /**
     * @Description 导出项分类枚举
     */
    public enum Type {
        PERSONAL(1, "客户"),

        JOB(2, "工作"),

        CONCAT(3, "联系人"),

        CASE(4, "案件"),

        BANK(5, "银行"),

        FOLLOW(6, "跟进记录");

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

    /**
     * @Description 类别分类枚举
     */
    public enum Category {
        INRUSH(1, "内催"),

        OUTSOURCE(2, "委外跟踪记录"),

        CASEUPDATE(3, "案件更新"),

        OUTSOURCEFOLLOWUP(4, "委外案件"),

        OUTSOURCECLOSEDFOLLOWUP(5, "委外已结案跟踪记录"),

        INNERCLOSEDFOLLOWUP(6, "内催已结案跟踪记录");


        private Integer value;
        private String remark;

        Category(Integer value, String remark) {
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
