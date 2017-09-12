package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by luqiang on 2017/8/2.
 */
@Data
@Document
@ApiModel(value = "CaseStrategy",
        description = "案件分配策略")
public class CaseStrategy {
    @Id
    private String id;
    //策略名称
    private String name;
    //创建人
    private String creator;
    //分配类型 0 机构 1 催收员
    private Integer assignType;
    //创建日期
    private Date createTime;
    //策略Json对象
    private String strategyJson;
    //策略公式
    private String strategyText;
    //分配给对应的催收员
    private List<String> users;
    //分配给对应的机构
    private List<String> departments;
    //优先级
    private Integer priority;
    //公司code码
    private String companyCode;
}
