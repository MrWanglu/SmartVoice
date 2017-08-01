package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.mapper.CaseInfoMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 18:07 2017/8/1
 */
@Service("caseInfoService")
public class CaseInfoService {
    @Autowired
    CaseInfoMapper caseInfoMapper;

    public List<CaseInfo> getAll(CaseInfo caseInfo){
        caseInfo=new CaseInfo();
        if (Objects.nonNull(caseInfo)&& caseInfo.getPage() != null && caseInfo.getRows() != null) {
            PageHelper.startPage(caseInfo.getPage(), caseInfo.getRows());
        }
       return caseInfoMapper.selectAll();
    }

}
