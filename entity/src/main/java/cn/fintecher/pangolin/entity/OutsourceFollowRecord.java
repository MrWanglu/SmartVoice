package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by huaynmin on 2017/9/26.
 * @Description : 委外跟进记录实体类
 */
@Entity
@Data
@Table(name = "outsource_follow_record")
public class OutsourceFollowRecord extends BaseEntity {


    @ApiModelProperty("公司标识符")
    private String companyCode;

    @ApiModelProperty(notes = "案件ID")
    @ManyToOne
    @JoinColumn(name="case_id")
    private CaseInfo caseInfo;

    @ApiModelProperty(notes = "案件编号")
    private String caseNum;

    @ApiModelProperty(notes = "跟进时间")
    private Date followTime;

    @ApiModelProperty(notes = "跟进方式")
    private String followType;

    @ApiModelProperty(notes = "催收对象")
    private String objectName;

    @ApiModelProperty(notes = "姓名")
    private String userName;

    @ApiModelProperty(notes = "电话状态")
    private String telStatus;

    @ApiModelProperty(notes = "催收反馈")
    private String feedback;

    @ApiModelProperty(notes = "跟进记录")
    private String followRecord;

    @ApiModelProperty(notes = "跟进人员")
    private String followPerson;

    @ApiModelProperty(notes = "导入时间")
    private Date importTime;



}
