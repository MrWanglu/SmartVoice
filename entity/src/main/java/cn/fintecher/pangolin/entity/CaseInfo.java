package cn.fintecher.pangolin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Document
@Data
public class CaseInfo {
    private @Id
    String id;
    private @DBRef
    PersonalInfo personalInfo;
    private @DBRef
    Operator operator;
    private @DBRef
    Domain domain;
    private @DBRef
    Department department;
}
