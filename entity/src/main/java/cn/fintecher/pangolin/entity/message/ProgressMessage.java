package cn.fintecher.pangolin.entity.message;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by ChenChang on 2017/3/31.
 */
@Data
public class ProgressMessage implements Serializable {
    private String userId;
    private String text;
    private Integer total;
    private Integer current;
}
