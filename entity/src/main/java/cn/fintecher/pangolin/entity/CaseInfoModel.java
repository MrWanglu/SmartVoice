package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by zzl029 on 2017/8/10.
 */
@Data
public class CaseInfoModel {
    @ApiModelProperty(notes = "案件id")
    private String cupoIdNew;

    @ApiModelProperty(notes = "部门编号")
    private String deptCodeNew;

    @ApiModelProperty(notes = "催收员")
    private String cupoUserNew;

    @ApiModelProperty(notes = "身份证号")
    private String IdCard;

    @ApiModelProperty(notes = "案件编号")
    private String cupoCaseNumNew;

    @ApiModelProperty(notes = "案件id")
    private String cupoIdOld;

    @ApiModelProperty(notes = "部门编号")
    private String deptCodeOld;

    @ApiModelProperty(notes = "催收员")
    private String cupoUserOld;

    @ApiModelProperty(notes = "分配类型，0，正常，1 分到对应的机构，2 分到对应的人")
    private Integer disCaseType;

    @ApiModelProperty(notes = "机构名称")
    private String deptNameOld;

    @ApiModelProperty(notes = "正常数据中的相同的身份证号的案件 0 相同 1 不同")
    private Integer sameIdcard;

    @ApiModelProperty(notes = "催收类型 69 电催 70 外访")
    private Integer cupoRectype;

    @ApiModelProperty(notes = "催收员名称")
    private String cupoUserNameOld;

    public enum DisCaseType {
        NORMAL(204), BYDEPT(205), BYUSER(206);
        private int id;

        DisCaseType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum SameIdCard {
        SAME(0), DIFF(1);
        private int id;

        SameIdCard(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
