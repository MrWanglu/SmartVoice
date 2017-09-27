package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.RecoverCaseParams;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoReturnRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by sunyanping on 2017/9/27.
 */
@Service
public class RecoverCaseService {

    private final Logger logger = LoggerFactory.getLogger(RecoverCaseService.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;

    public void recoverCase(RecoverCaseParams recoverCaseParams, User user) {
        if (recoverCaseParams.getIds().isEmpty()) {
            throw new RuntimeException("请选择要回收的案件!");
        }
        if (StringUtils.isBlank(recoverCaseParams.getReason())) {
            throw new RuntimeException("回收说明不能为空!");
        }
        try {
            Iterable<CaseInfo> all = caseInfoRepository.findAll(QCaseInfo.caseInfo.id.in(recoverCaseParams.getIds()));
            Iterator<CaseInfo> iterator = all.iterator();
            while (iterator.hasNext()) {
                CaseInfo caseInfo = iterator.next();
                caseInfo.setOperator(user);
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());
                caseInfoRepository.save(caseInfo);
                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setCaseId(caseInfo);
                caseInfoReturn.setReason(recoverCaseParams.getReason());
                caseInfoReturn.setOperator(user.getId());
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturnRepository.save(caseInfoReturn);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("操作失败!");
        }
    }

    public void recoverCase(Iterable<CaseInfo> all) {

    }
}
