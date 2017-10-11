package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.model.OutDistributeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huyanmin on 2017/10/11.
 * Description: 委外池service
 *
 */
@Service("outsourcePoolService")
public class OutsourcePoolService {
    final Logger log = LoggerFactory.getLogger(OutsourcePoolService.class);



    public List<OutDistributeInfo> distributePreview(AccCaseInfoDisModel accCaseInfoDisModel) {
        List<OutDistributeInfo> list = new ArrayList<>();

        return list;
    }


}
