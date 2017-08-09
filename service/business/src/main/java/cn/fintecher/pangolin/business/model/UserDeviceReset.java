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
    Set<String> userIds;
    Integer usdeType;
    Integer usdeStatus;
    Integer validate;

}
