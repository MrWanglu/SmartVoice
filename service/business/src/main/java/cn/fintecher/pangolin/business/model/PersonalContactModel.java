package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.PersonalContact;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 客户联系人模型
 * @Date : 11:55 2017/8/17
 */

@Data
public class PersonalContactModel {
    private List<PersonalContact> content;
}