package cn.fintecher.pangolin.common.web.rest;


import cn.fintecher.pangolin.common.service.SmaRequestService;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smaResource")
@ApiIgnore
public class SmaResource {
    private final Logger log = LoggerFactory.getLogger(SmaResource.class);
    @Autowired
    SmaRequestService smaRequestService;

    @PostMapping("/addTaskVoiceFileByTaskId")
    @ApiOperation(value = "调用sma接口保存录音文件", notes = "调用sma接口保存录音文件")
    public ResponseEntity<String> addTaskVoiceFileByTaskId(@RequestBody AddTaskVoiceFileMessage request) {
        Map paramMap = new HashMap();
        //查询录音文件id
        paramMap.put("taskid", request.getTaskid());
        //电签呼叫申请任务记录 ID
        paramMap.put("recoderId", request.getRecorderId());
        // 是当前坐席id
        paramMap.put("taskcallerid", request.getTaskcallerId());
        paramMap.put("salesmanCode", "");
        ResponseEntity<Map<String, String>> result = smaRequestService.smaRequest("addTaskVoiceFileBytaskId.html", paramMap);
        if (result.getStatusCode().is2xxSuccessful()) {
            Map<String, String> resultMap = result.getBody();
            log.info("{} 获取录音成功:{}", request.getRecorderId(), resultMap);
            return ResponseEntity.ok().body(resultMap.get("taskVoiceFileUrl"));
        } else {
            log.info("{} {}", request.getRecorderId(), result.getHeaders().get("X-pangolin-alert"));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
