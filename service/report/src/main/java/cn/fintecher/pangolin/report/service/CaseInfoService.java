package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.entity.CaseInfo;

import cn.fintecher.pangolin.report.mapper.CaseInfoMapper;
import cn.fintecher.pangolin.report.model.CaseInfoParams;
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

    public List<CaseInfo> queryWaitCollectCase(User user,Integer page, Integer size){
        List<CaseInfo> list = null;
        CaseInfoParams caseInfoParams = new CaseInfoParams();
        caseInfoParams.setCompanyCode(user.getCompanyCode());
        caseInfoParams.setCollector(user.getId());
        caseInfoParams.setDeptCode(user.getDepartment().getCode());
        PageHelper.startPage(page, size);
        if (user.getManager() == 1) {
            list = caseInfoMapper.queryWaitCollectCase(caseInfoParams);
        }else{
            list = caseInfoMapper.queryWaitOwnCollectCase(caseInfoParams);
        }
        return list;
    }

    public void updateLngLat(Personal personal){
        caseInfoMapper.updateLngLat(personal);
    }
}
