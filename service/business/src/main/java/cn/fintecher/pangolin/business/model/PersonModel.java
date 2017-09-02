package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/1.
 */
@Data
public class PersonModel {
    private String personalId;
    private List<String> personConcents;
}
