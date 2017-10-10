package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;
/**
 * Created by Administrator on 2017/10/10.
 */
@Data
public class OutCodeList {
    private List<String> outCode; //委外编码list
    private String companyCode;
}
