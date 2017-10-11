package cn.fintecher.pangolin.dataimp.model;

import lombok.Data;

/**
 * Created by zzl029 on 2017/8/10.
 */
@Data
public class JsonObj {
    private String jsonStr;
    private Integer strategyType; //策略类型：230-导入案件分配策略，231-内催池案件分配策略，232-委外池案件分配策略
}
