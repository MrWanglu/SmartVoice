package cn.fintecher.pangolin.common.client;

import cn.fintecher.pangolin.entity.DataDict;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-27-13:28
 */
@FeignClient("business-service")
public interface DataDictClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/dataDictResource")
    ResponseEntity<List<DataDict>> getDataDictByTypeCode(@RequestParam(value = "typeCode") String typeCode);
}
