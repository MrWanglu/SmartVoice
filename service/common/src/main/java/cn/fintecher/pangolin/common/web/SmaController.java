package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.DataDictClient;
import cn.fintecher.pangolin.common.client.SysParamClient;
import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.AddTaskRecorderRequest;
import cn.fintecher.pangolin.common.model.BindCallNumberRequest;
import cn.fintecher.pangolin.common.service.CallService;
import cn.fintecher.pangolin.common.service.SmaRequestService;
import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.MD5;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
    private static final String ENTITY_NAME = "Sma";
    @Autowired
    SmaRequestService smaRequestService;
    @Autowired
    RestTemplate restTemplate;
    @Value("${pangolin.sma.sysTarget}")
    private String sysTarget;
    @Autowired
    private UserClient userClient;
    @Autowired
    private DataDictClient dataDictClient;
    @Autowired
    private SysParamClient sysParamClient;
    @Autowired
    private CallService callService;
    //中通天鸿参数配置
    @Value("${pangolin.zhongtong-server.cti}")
    private String cti;

    /**
     * @Description : 呼叫类型设置
     */
    @GetMapping("/getSmaType")
    @ApiOperation(value = "呼叫类型", notes = "呼叫类型")
    public ResponseEntity<List<DataDict>> getSmaType() {
        ResponseEntity<List<DataDict>> dataDict = dataDictClient.getDataDictByTypeCode("0038");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDict.getBody());
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @GetMapping("/validateTaskIdInEmpId")
    @ApiOperation(value = "验证呼叫ID是否绑定", notes = "验证呼叫ID是否绑定")
    public ResponseEntity<Map<String, String>> validateTaskIdInEmpId(@RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
        //呼叫中心配置
        SysParam sysParam = sysParamClient.getSysParamByCodeAndType(user.getId(), user.getCompanyCode(), Constants.PHONE_CALL_CODE, Constants.PHONE_CALL_TYPE).getBody();
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "未获取呼叫配置的系统参数", "Did not get call configuration of system parameters")).body(null);
        }
        // 163 erpv3   164  中通天鸿  165  云羿
        if (Objects.equals("163", sysParam.getValue())) {
            Map paramMap = new HashMap();
            paramMap.put("empId", user.getId());
            return smaRequestService.smaRequest("validateTaskIdInEmpid.html", paramMap);
        }
        //164  中通天鸿 对呼绑定 在user中的callPhone 字段
        if (Objects.equals("164", sysParam.getValue()) || Objects.equals("165", sysParam.getValue())) {
            if (Objects.nonNull(user.getCallPhone())) {
                Map paramMap = new HashMap();
                paramMap.put("callPhone", user.getCallPhone());
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Is binding", "已经绑定")).body(paramMap);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unbounded calling number", "未绑定主叫号码")).body(null);
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);
    }

    @PostMapping("/bindTaskDataByCallerId")
    @ApiOperation(value = "绑定呼叫ID", notes = "呼叫信息")
    public ResponseEntity<Map<String, String>> bindTaskDataByCallerId(@RequestBody BindCallNumberRequest request, @RequestHeader(value = "X-UserToken") String token) {
        // 是否登录
        User user = userClient.getUserByToken(token).getBody();

        //呼叫中心配置
        SysParam sysParam = sysParamClient.getSysParamByCodeAndType(user.getId(), user.getCompanyCode(), Constants.PHONE_CALL_CODE, Constants.PHONE_CALL_TYPE).getBody();
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "未获取呼叫配置的系统参数", "Did not get call configuration of system parameters")).body(null);
        }
        // 163 erpv3   164  中通天鸿  165  云羿
        if (Objects.equals("163", sysParam.getValue())) {
            Map paramMap = new HashMap();
            paramMap.put("empId", user.getId());
            paramMap.put("callerid", request.getCallerId());//固定话机ID
            paramMap.put("salesmanCode", user.getRealName() + user.getUserName());
            paramMap.put("caller", request.getCaller());//主叫号码选填
            return smaRequestService.smaRequest("bindTaskDataByCallerid.html", paramMap);
        }
        //164  中通天鸿 对呼绑定 在user中的callPhone 字段
        if (Objects.equals("164", sysParam.getValue()) || Objects.equals("165", sysParam.getValue())) {
            if (Objects.nonNull(user.getCallPhone())) {
                Map paramMap = new HashMap();
                paramMap.put("callPhone", user.getCallPhone());
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Is binding", "已经绑定")).body(paramMap);
            } else {
                user.setCallPhone(request.getCaller());
                User user1 = userClient.saveUser(user).getBody();
                Map paramMap1 = new HashMap();
                paramMap1.put("callPhone", user1.getCallPhone());
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Binding success", "绑定成功")).body(paramMap1);
            }
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);

    }

    @PostMapping("/addTaskRecorder")
    @ApiOperation(value = "开始电话呼叫", notes = "开始电话呼叫")
    public ResponseEntity<Map<String, String>> addTaskRecorder(@RequestBody AddTaskRecorderRequest request,
                                                               @RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
//        呼叫中心配置
        SysParam sysParam = sysParamClient.getSysParamByCodeAndType(user.getId(), user.getCompanyCode(), Constants.PHONE_CALL_CODE, Constants.PHONE_CALL_TYPE).getBody();
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
        }

//         163 erpv3     165  云羿
        if (Objects.equals("163", sysParam.getValue())) {
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
//        164  中通天鸿
        if (Objects.equals("164", sysParam.getValue())) {
            if (Objects.isNull(user.getCallPhone())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User does not bind the main call number", "用户未绑定主叫号码")).body(null);
            }
            request.setCaller(user.getCallPhone());
            if (Objects.isNull(request.getCallee())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Get the called number failed", "获取被叫号码失败")).body(null);
            }
            HttpClient client = new HttpClient();
            client.setConnectionTimeout(1000 * 60);
            client.getHostConfiguration().setHost(cti, 80, "http");
            HttpMethod method = callService.getPostMethod(request);
            try {
                client.executeMethod(method);
            /* getResponseBodyAsStream start */
                InputStream inputStream = method.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer response = new StringBuffer();
                String read = "";
                while ((read = br.readLine()) != null) {
                    response.append(read);
                }
//            System.out.println(response);
        /* getResponseBodyAsStream start */
                method.releaseConnection();
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "be defeated", "失败")).body(null);
            }
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);
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
