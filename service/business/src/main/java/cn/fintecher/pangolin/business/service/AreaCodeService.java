package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.AreaCodeRepository;
import cn.fintecher.pangolin.entity.AreaCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:06 2017/7/28
 */
@Service("areaCodeService")
public class AreaCodeService {

    @Autowired
    AreaCodeRepository areaCodeRepository;

    /**
     * 查询所有的区域信息
     * @return
     */
    @Cacheable(value = "areaCodes", key = "'pangolin:areaCode:all'", unless = "#result==null")
    public Iterable<AreaCode> queryAllAreaCode(){
        return areaCodeRepository.findAll();
    }

    /**
     * 通过名字查找区域信息
     * @param areaName
     * @return
     */
    @Cacheable(value = "areaCode", key = "'pangolin:areaCode:areaName'", unless = "#result==null")
    public AreaCode queryAreaCodeByName(String areaName){
        Iterable<AreaCode> areaCodeIterable=queryAllAreaCode();
        for(Iterator<AreaCode> it=areaCodeIterable.iterator();it.hasNext();){
            AreaCode areaCode=it.next();
            if(areaName.contains(areaCode.getAreaName()) || areaCode.getAreaName().contains(areaName)){
                return areaCode;
            }
        }
        return null;
    }

    /**
     * 通过ID查找
     * @param id
     * @return
     */
    @Cacheable(value = "areaCode", key = "'pangolin:areaCode:id'", unless = "#result==null")
    public AreaCode queryAreaCodeById(Integer id){
      return  areaCodeRepository.findOne(id);
    }
}
