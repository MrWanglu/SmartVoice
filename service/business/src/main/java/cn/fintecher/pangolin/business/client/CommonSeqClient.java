package cn.fintecher.pangolin.business.client;

import cn.fintecher.pangolin.entity.util.LabelValue;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:26 2017/7/19
 */
@FeignClient("bussines-service")
public interface CommonSeqClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/commonSeqResource/getNextSeq")
    ResponseEntity<LabelValue> getNextSeq(@RequestParam @ApiParam("序列名称") String name, @RequestParam @ApiParam("序列长度") Integer length);

}
