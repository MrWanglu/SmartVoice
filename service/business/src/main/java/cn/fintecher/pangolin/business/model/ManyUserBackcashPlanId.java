package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-02-13:49
 */
@Data
public class ManyUserBackcashPlanId {
    private List<String> ids; //用户计划回款ids
}
