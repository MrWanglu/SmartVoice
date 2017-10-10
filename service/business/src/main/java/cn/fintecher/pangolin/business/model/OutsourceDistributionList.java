package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.OutsourceRecord;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/10/10.
 */
@Data
public class OutsourceDistributionList {
    private List<OutsourcePool> outsourcePools;
    private List<OutsourceRecord> outsourceRecords;
}
