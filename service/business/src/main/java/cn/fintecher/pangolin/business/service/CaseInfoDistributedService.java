package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: PeiShouWen
 * @Description: 案件分配服务类
 * @Date 16:38 2017/8/7
 */
@Service("caseInfoDistributedService")
public class CaseInfoDistributedService {

    Logger logger= LoggerFactory.getLogger(CaseInfoDistributedService.class);

    @Autowired
    CaseInfoDistributedRepository caseInfoDistributedRepository;


}
