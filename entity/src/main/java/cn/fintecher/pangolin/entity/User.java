package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table
@Data
public class User extends BaseEntity {
    private Department department;
    private List<Role> roles;
}
