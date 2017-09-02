package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/1.
 */
@Data
public class CapaMessageParams {
    List<String> caseNumbers;
    List<String> personalIds;
    List<CapaPersons> personalParamsList;
    String sendType;
    String tesmId;
}
