package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.PersonalAddress;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 客户联系人地址模型
 * @Date : 14:16 2017/8/18
 */

@Data
public class PersonalAddressModel {
    private List<PersonalAddress> content;
}