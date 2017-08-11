package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.ResourceRepository;
import cn.fintecher.pangolin.entity.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-10-13:34
 */
@Service("resourceService")
public class ResourceService {
    final Logger log = LoggerFactory.getLogger(ResourceService.class);
    @Autowired
    private ResourceRepository resourceRepository;

    @Cacheable(value = "resourceCache", key = "'petstore:resource:all'", unless = "#result==null")
    public Iterable<Resource> findAll() {
        return resourceRepository.findAll();
    }

    @Cacheable(value = "resourceCache", key = "'petstore:resource:all'", unless = "#result==null")
    public Resource save(Resource object) {
        return resourceRepository.save(object);
    }
}
