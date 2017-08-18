package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-06-10:12
 */
@Data
public class BatchSendRequest {
    private List<String> cupoIdList = new ArrayList<>(); //案件id集合
    private Integer selected; //是否选择本人
    private List<Integer> selRelationsList = new ArrayList<>(); //联系人集合
}
