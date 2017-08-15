package cn.fintecher.pangolin.dataimp.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ChenChang on 2017/3/17.
 */
@Data
public class ListResult<E> implements Serializable {
    private String user;
    private List<E> result;
    private Integer status;

    public enum Status {
        // 0-成功，1-失败
        SUCCESS(0), FAILURE(1);
        private Integer val;

        Status(Integer val) {
            this.val = val;
        }

        public Integer getVal() {
            return val;
        }
    }
}
