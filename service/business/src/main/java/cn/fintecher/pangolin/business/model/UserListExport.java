package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.User;
import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-16-11:43
 */
@Data
public class UserListExport {
    List<User> userList;
    List<UserModel> userModelList;
}
