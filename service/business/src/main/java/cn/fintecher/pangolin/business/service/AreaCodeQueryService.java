package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.AreaCodeRepository;
import cn.fintecher.pangolin.entity.AreaCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @Author: Duchao
 * @Description:
 * @Date 10:06 2017/9/5
 */
@Service("areaCodeQueryService")
public class AreaCodeQueryService {

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
     * 通过ID查找
     * @param id
     * @return
     */
    @Cacheable(value = "areaCode", key = "'pangolin:areaCode:id'", unless = "#result==null")
    public AreaCode queryAreaCodeById(Integer id){
        return  areaCodeRepository.findOne(id);
    }
}
