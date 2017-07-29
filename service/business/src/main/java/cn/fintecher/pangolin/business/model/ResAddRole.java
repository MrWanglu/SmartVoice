package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @Author: hukaijia
 * @Description:
 * @Date 2017/3/23
 */
@Data
public class ResAddRole {
    List<String> resoIds;
    String roleId;
}
