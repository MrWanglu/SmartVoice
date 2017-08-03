package cn.fintecher.pangolin.business.model;


import cn.fintecher.pangolin.entity.UserBackcashPlan;
import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 封装方法的参数
 * @Date : 2017/6/7.
 */
@Data
public class BackPlanImportParams {
    private String localUrl;
    private String token;
    private List<String> usernameList;
    private List<UserBackcashPlan> userPlan;
    private int[] startRow;
    private int[] startCol;
    private Class<?>[] dataClass;
}
