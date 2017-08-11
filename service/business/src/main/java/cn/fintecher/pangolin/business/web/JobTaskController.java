package cn.fintecher.pangolin.business.web;

/**
 * @Author: PeiShouWen
 * @Description: 任务调度
 * @Date 15:21 2017/8/10
 */

import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobTaskController")
@Api(value = "JobTaskController", description = "任务调度")
public class JobTaskController extends BaseController {
    private final Logger logger= LoggerFactory.getLogger(JobTaskController.class);
    private static final String ENTITY_NAME = "JobTaskController";


    public ResponseEntity updateOverNightJob(@RequestBody SysParam sysParam ,
                                             @RequestHeader(value = "X-UserToken") String token){
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"updateOverNightJob","更新成功")).body(null);
    }
}
