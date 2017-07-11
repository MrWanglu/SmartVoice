package cn.fintecher.pangolin.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Document
@Data
public class Operator {
    private @Id
    String id;
    private @DBRef
    Department department;
    private @DBRef
    List<Role> roles;
}
