package cn.fintecher.pangolin.common.client;

import cn.fintecher.pangolin.entity.SysParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-27-14:15
 */
@FeignClient("business-service")
public interface SysParamClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/sysParamResource")
    ResponseEntity<SysParam> getSysParamByCodeAndType(@RequestParam(value = "userId") String userId,
                                                      @RequestParam(value = "companyCode") String companyCode,
                                                      @RequestParam(value = "code") String code,
                                                      @RequestParam(value = "type") String type);
}
