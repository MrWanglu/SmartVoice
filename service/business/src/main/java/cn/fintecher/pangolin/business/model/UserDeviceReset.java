package cn.fintecher.pangolin.business.model;

import lombok.Data;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Created by yuanyanting on 2017/7/31.
 */

@Entity
@Data
public class UserDeviceReset {

    /** 用户的ID集合 */
    Set<String> userIds;

    /** 设备类型（0：PC端，1：移动端） */
    Integer usdeType;

    /** 用户状态（0：启用，1：禁用） */
    Integer usdeStatus;

    /** 用户设备验证（0：开启，1：停用） */
    Integer validate;

}
