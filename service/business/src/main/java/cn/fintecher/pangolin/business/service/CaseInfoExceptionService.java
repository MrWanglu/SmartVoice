package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.entity.CaseInfoException;
import cn.fintecher.pangolin.entity.QCaseInfoException;
import cn.fintecher.pangolin.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: PeiShouWen
 * @Description: 案件异常池服务类
 * @Date 16:28 2017/8/7
 */
@Service("caseInfoExceptionService")
public class CaseInfoExceptionService {

    @Autowired
    CaseInfoExceptionRepository caseInfoExceptionRepository;

    /**
     * 检查时候有异常案件未处理
     * 查询所有未处理的异常案件
     * @return
     */
    public boolean checkCaseExceptionExist(User user){
        QCaseInfoException qCaseInfoException=QCaseInfoException.caseInfoException;
        return caseInfoExceptionRepository.exists(qCaseInfoException.companyCode.eq(user.getCompanyCode())
                .and(qCaseInfoException.repeatStatus.eq(CaseInfoException.RepeatStatusEnum.PENDING.getValue())));
    }
}
