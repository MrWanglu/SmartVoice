package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.AddTaskRecorderRequest;
import cn.fintecher.pangolin.common.model.BindCallNumberRequest;
import cn.fintecher.pangolin.common.service.SmaRequestService;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import cn.fintecher.pangolin.entity.util.MD5;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by mincx
 * <p/>
 * 2017/3/23.
 */
@RestController
@RequestMapping(value = "/api/smaController")
@Api(value = "呼叫相关接口", description = "呼叫相关接口")
public class SmaController {
    private static final Logger logger = LoggerFactory.getLogger(SmaController.class);
    @Autowired
    SmaRequestService smaRequestService;
    @Autowired
    RestTemplate restTemplate;
    @Value("${pangolin.sma.sysTarget}")
    private String sysTarget;
    @Autowired
    private UserClient userClient;

    @GetMapping("/validateTaskIdInEmpId")
    @ApiOperation(value = "验证呼叫ID是否绑定", notes = "验证呼叫ID是否绑定")
    public ResponseEntity<Map<String, String>> validateTaskIdInEmpId(@RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
        Map paramMap = new HashMap();
        paramMap.put("empId", user.getId());
        return smaRequestService.smaRequest("validateTaskIdInEmpid.html", paramMap);
    }

    @PostMapping("/bindTaskDataByCallerId")
    @ApiOperation(value = "绑定呼叫ID", notes = "呼叫信息")
    public ResponseEntity<Map<String, String>> bindTaskDataByCallerId(@RequestBody BindCallNumberRequest request, @RequestHeader(value = "X-UserToken") String token) {
        // 是否登录
        User user = userClient.getUserByToken(token).getBody();
        Map paramMap = new HashMap();
        paramMap.put("empId", user.getId());
        paramMap.put("callerid", request.getCallerId());//固定话机ID
        paramMap.put("salesmanCode", user.getRealName() + user.getUserName());
        paramMap.put("caller", request.getCaller());//主叫号码选填
        return smaRequestService.smaRequest("bindTaskDataByCallerid.html", paramMap);
    }

    @PostMapping("/addTaskRecorder")
    @ApiOperation(value = "开始电话呼叫", notes = "开始电话呼叫")
    public ResponseEntity<Map<String, String>> addTaskRecorder(@RequestBody AddTaskRecorderRequest request, @RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
        Map paramMap = new HashMap();
        paramMap.put("id", request.getTaskId());//呼叫流程id
        paramMap.put("caller", request.getCaller());//主叫号码
        paramMap.put("callee", request.getCallee());//被叫号码
        paramMap.put("empId", user.getId());//业务员ID
        paramMap.put("applyId", "");//信贷借款id
        paramMap.put("custInfoId", MD5.MD5Encode(request.getCustomer()));//客户ID
        paramMap.put("sysTarget", sysTarget);//ACC贷后，Review评审
        paramMap.put("salesmanCode", user.getRealName() + user.getUserName());
        return smaRequestService.smaRequest("addTaskRecoder.html", paramMap);
    }

    @PostMapping("/addTaskVoiceFileByTaskId")
    @ApiOperation(value = "调用sma接口保存录音文件", notes = "调用sma接口保存录音文件")
    public ResponseEntity<Map<String, String>> addTaskVoiceFileByTaskId(@RequestBody AddTaskVoiceFileMessage request) {
        Map paramMap = new HashMap();
        paramMap.put("taskid", request.getTaskid());        //获取 下载录音文件的 流程id
        paramMap.put("recoderId", request.getRecorderId());            //电签呼叫申请任务记录 ID
        paramMap.put("taskcallerid", request.getTaskcallerId());            //坐席号 ID
        paramMap.put("salesmanCode", "");
        return smaRequestService.smaRequest("addTaskVoiceFileBytaskId.html", paramMap);
    }

    @GetMapping("/getVoice")
    @ApiOperation(value = "查询客户id的呼叫所有录音", notes = "呼叫信息")
    public ResponseEntity<Map<String, String>> getVoice(@RequestParam String customerId, Pageable pageable, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        Map paramMap = new HashMap();
        paramMap.put("custInfoId", customerId); //客户id
        paramMap.put("pageNo", pageable.getPageNumber());  //分页 页码
        paramMap.put("pageSize", pageable.getPageSize()); //分页 页数
        //验证 当前登陆者 是否绑定了 呼叫流程id
        return smaRequestService.smaRequest("getTaskRecoders.html", paramMap);
    }
}
