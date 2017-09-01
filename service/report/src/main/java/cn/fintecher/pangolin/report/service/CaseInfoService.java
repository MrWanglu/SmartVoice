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

    public List<CaseInfo> queryWaitCollectCase(CaseInfoParams caseInfoParams,int page, int size,User user){
        List<CaseInfo> list = null;
        PageHelper.startPage(page, size);
        if (Objects.equals(user.getManager(),User.MANAGER_TYPE.DATA_AUTH)) {
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
