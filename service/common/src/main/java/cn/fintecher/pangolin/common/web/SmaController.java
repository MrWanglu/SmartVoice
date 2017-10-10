package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.DataDictClient;
import cn.fintecher.pangolin.common.client.SysParamClient;
import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.AddTaskRecorderRequest;
import cn.fintecher.pangolin.common.model.BindCallNumberRequest;
import cn.fintecher.pangolin.common.service.CallService;
import cn.fintecher.pangolin.common.service.SmaRequestService;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.MD5;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.alibaba.fastjson.JSONObject;
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

import java.io.*;
import java.net.Socket;
import java.util.*;


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
    @Value("${pangolin.zhongtong-server.recordlist}")
    private String recordlist;
    //云翳参数配置
    @Value("${pangolin.yunyi-server.host}")
    private String host;
    @Value("${pangolin.yunyi-server.port}")
    private int port;
    @Value("${pangolin.yunyi-server.timeout}")
    private int timeout;
    @Value("${pangolin.yunyi-server.customerDisplayNum}")
    private String customerDisplayNum;
    @Value("${pangolin.yunyi-server.agentPwd}")
    private String agentPwd;

    // 汉天参数
    @Value("${hantian-server.callengine}")
    private String callengine;

    @Value("${hantian-server.token}")
    private String hantianToken;

    /**
     * @Description : 呼叫类型设置
     */
    @GetMapping("/getSmaType")
    @ApiOperation(value = "呼叫类型", notes = "呼叫类型")
    public ResponseEntity<List<DataDict>> getSmaType() {
        ResponseEntity<List<DataDict>> dataDict = dataDictClient.getDataDictByTypeCode("0038");
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取成功", "")).body(dataDict.getBody());
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @GetMapping("/validateTaskIdInEmpId")
    @ApiOperation(value = "验证呼叫ID是否绑定", notes = "验证呼叫ID是否绑定")
    public ResponseEntity<Map<String, String>> validateTaskIdInEmpId(@RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = userClient.getUserByToken(token).getBody();
            //呼叫中心配置
            SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + user.getCompanyCode() + "&code=" + Constants.PHONE_CALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysParam)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            // 163 erpv3   164  中通天鸿  165  云羿
            if (Objects.equals(CaseFollowupRecord.CallType.ERPV3.getValue().toString(), sysParam.getValue())) {
                Map paramMap = new HashMap();
                paramMap.put("empId", user.getId());
                return smaRequestService.smaRequest("validateTaskIdInEmpid.html", paramMap);
            }
            //164  中通天鸿 对呼绑定 在user中的callPhone 字段
            if (Objects.equals(CaseFollowupRecord.CallType.TIANHONG.getValue().toString(), sysParam.getValue()) || Objects.equals(CaseFollowupRecord.CallType.YUNYI.getValue().toString(), sysParam.getValue()) ||
                    Objects.equals(CaseFollowupRecord.CallType.HANTIAN.getValue().toString(),sysParam.getValue())) {
                if (Objects.nonNull(user.getCallPhone())) {
                    Map paramMap = new HashMap();
                    paramMap.put("callPhone", user.getCallPhone());
                    return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Is binding", "已经绑定")).body(paramMap);
                }
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unbounded calling number", "未绑定主叫号码")).body(null);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
        }
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @PostMapping("/bindTaskDataByCallerId")
    @ApiOperation(value = "绑定呼叫ID", notes = "呼叫信息")
    public ResponseEntity<Map<String, String>> bindTaskDataByCallerId(@RequestBody BindCallNumberRequest request, @RequestHeader(value = "X-UserToken") String token) {
        try {
            // 是否登录
            User user = userClient.getUserByToken(token).getBody();
            //呼叫中心配置
            SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + user.getCompanyCode() + "&code=" + Constants.PHONE_CALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysParam)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            // 163 erpv3   164  中通天鸿  165  云羿
            if (Objects.equals(CaseFollowupRecord.CallType.ERPV3.getValue().toString(), sysParam.getValue())) {
                Map paramMap = new HashMap();
                paramMap.put("empId", user.getId());
                paramMap.put("callerid", request.getCallerId());//固定话机ID
                paramMap.put("salesmanCode", user.getRealName() + user.getUserName());
                paramMap.put("caller", request.getCaller());//主叫号码选填
                return smaRequestService.smaRequest("bindTaskDataByCallerid.html", paramMap);
            }
            //164  中通天鸿 对呼绑定 在user中的callPhone 字段
            if (Objects.equals(CaseFollowupRecord.CallType.TIANHONG.getValue().toString(), sysParam.getValue()) || Objects.equals(CaseFollowupRecord.CallType.YUNYI.getValue().toString(), sysParam.getValue()) ||
                    Objects.equals(CaseFollowupRecord.CallType.HANTIAN.getValue().toString(),sysParam.getValue())) {
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
        }
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @PostMapping("/addTaskRecorder")
    @ApiOperation(value = "开始电话呼叫", notes = "开始电话呼叫")
    public ResponseEntity<Map<String, String>> addTaskRecorder(@RequestBody AddTaskRecorderRequest request,
                                                               @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = userClient.getUserByToken(token).getBody();
//        呼叫中心配置
            SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + request.getCompanyCode() + "&code=" + Constants.PHONE_CALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysParam)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            //是否虚拟拨打 若是，则直接返回拨打成功
            SysParam sysObj = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + request.getCompanyCode() + "&code=" + Constants.PHONE_ISREALCALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysObj)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            Integer status = sysObj.getStatus();
            if (Objects.nonNull(status) && 0 == status){//虚拟拨打(此处的值写死)
                Map falseMap = new HashMap();
                falseMap.put("id", request.getTaskId());//呼叫流程id
                falseMap.put("resultTaskId", "61925452-cbcc-490c-acfd-7e6994a69d68");
                falseMap.put("taskId", "603881051");
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(falseMap);
            }

//         163 erpv3     165  云羿
            if (Objects.equals(CaseFollowupRecord.CallType.ERPV3.getValue().toString(), sysParam.getValue())) {
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
            if (Objects.equals(CaseFollowupRecord.CallType.TIANHONG.getValue().toString(), sysParam.getValue())) {
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
                    //解析交给前端，这边比较麻烦
                    Map<String, String> map = new HashMap<>();
                    map.put("response", response.toString());
        /* getResponseBodyAsStream start */
                    method.releaseConnection();
                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(map);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "be defeated", "失败")).body(null);
                }
            }
            //        165 云羿呼叫中心
            if (Objects.equals(CaseFollowupRecord.CallType.YUNYI.getValue().toString(), sysParam.getValue())) {
                if (Objects.isNull(user.getCallPhone())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User does not bind the main call number", "用户未绑定主叫号码")).body(null);
                }
                try {
                    Socket socket = new Socket(host, port);
                    socket.setSoTimeout(timeout);
                    BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

                    //拨打电话
                    String sendData1 = "<request><cmdType>manual_callout</cmdType><agentID>" + user.getCallPhone() + "</agentID><customerNum>" + request.getCallee() + "</customerNum><customerDisplayNum>"+customerDisplayNum+"</customerDisplayNum></request>";
                    String sendDataUtf821 = new String(sendData1.getBytes("UTF-8"), "UTF-8");
                    String head21 = "<<<length=" + sendDataUtf821.getBytes("UTF-8").length + ">>>";
                    sendDataUtf821 = head21 + sendDataUtf821;
                    pw.print(sendDataUtf821);
                    pw.flush();
                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(null);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "be defeated", "失败")).body(null);
                }
            }
            //    233 汉天呼叫中心
            if (Objects.equals(CaseFollowupRecord.CallType.HANTIAN.getValue().toString(), sysParam.getValue())) {
                if (Objects.isNull(user.getCallPhone())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User does not bind the main call number", "用户未绑定主叫号码")).body(null);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("command","dial");
                jsonObject.put("src",user.getCallPhone()); //主叫号码
                jsonObject.put("dest",request.getCallee()); //被叫号码
                jsonObject.put("ext_field", UUID.randomUUID().toString());
                Map<String, Object> vars = new HashMap<>();
                vars.put("json",jsonObject.toJSONString());

                String callbackP2PUrl = callengine + "?json={json}";
                RestTemplate hantianRest = new RestTemplate();
                String result = hantianRest.getForObject(callbackP2PUrl,String.class,vars);
                JSONObject resultJson = JSONObject.parseObject(result);
                if(resultJson.getIntValue("code") ==  0){
                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(null);
                }else {
                    logger.error("汉天呼叫系统呼叫失败:" + resultJson.toJSONString());
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
                }
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
        }
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @PostMapping("/addTaskVoiceFileByTaskId")
    @ApiOperation(value = "调用sma接口保存录音文件", notes = "调用sma接口保存录音文件")
    public ResponseEntity<Map<String, String>> addTaskVoiceFileByTaskId(@RequestBody AddTaskVoiceFileMessage request) {
        try {
            Map paramMap = new HashMap();
            paramMap.put("taskid", request.getTaskid());        //获取 下载录音文件的 流程id
            paramMap.put("recoderId", request.getRecorderId());            //电签呼叫申请任务记录 ID
            paramMap.put("taskcallerid", request.getTaskcallerId());            //坐席号 ID
            paramMap.put("salesmanCode", "");
            return smaRequestService.smaRequest("addTaskVoiceFileBytaskId.html", paramMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
        }
    }

    /**
     * @Description : v3系统的话绑定的是座机 163，需要添加中通天鸿 164 和云羿的呼叫系统 165
     */
    @GetMapping("/getVoice")
    @ApiOperation(value = "查询客户id的呼叫所有录音", notes = "呼叫信息")
    public ResponseEntity<Map<String, String>> getVoice(@RequestParam String customerId, Pageable pageable, @RequestHeader(value = "X-UserToken") String token) {
        Map paramMap = new HashMap();
        paramMap.put("custInfoId", customerId); //客户id
        paramMap.put("pageNo", pageable.getPageNumber());  //分页 页码
        paramMap.put("pageSize", pageable.getPageSize()); //分页 页数
        //验证 当前登陆者 是否绑定了 呼叫流程id
        return smaRequestService.smaRequest("getTaskRecoders.html", paramMap);
    }

    /**
     * @Description : v3系统 163，中通天鸿 164 云羿 165   验证呼叫来源
     */
    @GetMapping("/checkCall")
    @ApiOperation(value = "验证呼叫来源", notes = "验证呼叫来源")
    public ResponseEntity<SysParam> checkCall(@RequestParam String companyCode,
                                              @RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
        //        呼叫中心配置
        SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + companyCode + "&code=" + Constants.PHONE_CALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Is binding", "已经绑定")).body(sysParam);
    }

    /**
     * @Description : 云羿 165  打电话需要先签入动作  签入的话  目前是通过前端来限制的
     */
    @PostMapping("/signIn")
    @ApiOperation(value = "签入", notes = "签入")
    public ResponseEntity<Map<String, String>> signIn(@RequestBody User request,
                                                      @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = restTemplate.getForEntity("http://business-service/api/userResource/findUserById?id=" + request.getId(), User.class).getBody();
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Check-in user does not exist", "签入用户不存在")).body(null);
            }
            Map<String, String> map = callService.signIn(user.getId(), user.getCallPhone());
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(timeout);
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            // 签入
            StringBuilder stringBuilder = new StringBuilder();
            String response = "<request>\n" +
                    "<cmdType>signin</cmdType>\n" +
                    "<agentID>" + user.getCallPhone() + "</agentID>\n" +
                    "<agentPwd>"+agentPwd+"</agentPwd>\n" +
                    "<bindExten>yes</bindExten>\n" +
                    "<agentExten>" + user.getCallPhone() + "</agentExten>\n" +
                    "<initStatus>0</initStatus>\n" +
                    "</request>";
            stringBuilder.append(response);
            String sendDataUtf8 = new String(stringBuilder.toString().getBytes("UTF-8"), "UTF-8");
            String head = "<<<length=" + sendDataUtf8.getBytes("UTF-8").length + ">>>";
            sendDataUtf8 = head + sendDataUtf8;
            System.out.println("签入成功：" + sendDataUtf8);
            pw.print(sendDataUtf8);
            pw.flush();
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Checkin success", "签入成功")).body(map);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Checkin failure", "签入失败")).body(null);
        }
    }

    /**
     * @Description : 云羿 165  打完电话需要签出动作  签出 目前是通过前端来限制的
     */
    @PostMapping("/signOut")
    @ApiOperation(value = "签出", notes = "签出")
    public ResponseEntity<Map<String, String>> signOut(@RequestBody User request,
                                                       @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = restTemplate.getForEntity("http://business-service/api/userResource/findUserById?id=" + request.getId(), User.class).getBody();
            Map<String, String> map1 = Constants.map;
            if (map1.containsKey(user.getId())) {
                Map<String, String> map = callService.signOut(user.getId(), user.getCallPhone());
                Socket socket = new Socket(host, port);
                socket.setSoTimeout(timeout);
                BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                // 签入
                String sendData = "<request><cmdType>signout</cmdType><agentID>" + user.getCallPhone() + "</agentID><reason></reason></request>";
                String sendDataUtf8 = new String(sendData.getBytes("UTF-8"), "UTF-8");
                String head = "<<<length=" + sendDataUtf8.getBytes("UTF-8").length + ">>>";
                sendDataUtf8 = head + sendDataUtf8;
                System.out.println("签出成功：" + sendDataUtf8);
                pw.print(sendDataUtf8);
                pw.flush();
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Check out the success", "签出成功")).body(map);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, " success", "成功")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Checkin failure", "签入失败")).body(null);
        }
    }

    /**
     * @Description : 中通天鸿 164 获取通话记录
     */
    @GetMapping("/getTianHongVoice")
    @ApiOperation(value = "用于查询双向外呼通话记录", notes = "用于查询双向外呼通话记录")
    public ResponseEntity<String> getTianHongVoice() {
        try {
            HttpClient client = new HttpClient();
            client.setConnectionTimeout(1000 * 60);
            client.getHostConfiguration().setHost(recordlist, 80, "http");
            HttpMethod method = callService.getAllVoice();
            client.executeMethod(method);
            String jsonStr = method.getResponseBodyAsString();
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(jsonStr);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "失败")).body(null);
        }
    }


    /**
     * @Description : 233 汉天双向回拨-呼叫
     */
    @PostMapping("/hantianP2P")
    @ApiOperation(value = "汉天双向回拨-开始呼叫", notes = "汉天双向回拨-开始呼叫")
    public ResponseEntity<Map<String, String>> hantianP2P(@RequestBody AddTaskRecorderRequest request,
                                                               @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = userClient.getUserByToken(token).getBody();
//        呼叫中心配置
            SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + request.getCompanyCode() + "&code=" + Constants.PHONE_CALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysParam)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            //是否虚拟拨打 若是，则直接返回拨打成功
            SysParam sysObj = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + user.getId() + "&companyCode=" + request.getCompanyCode() + "&code=" + Constants.PHONE_ISREALCALL_CODE + "&type=" + Constants.PHONE_CALL_TYPE, SysParam.class).getBody();
            if (Objects.isNull(sysObj)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Did not get call configuration of system parameters", "未获取呼叫配置的系统参数")).body(null);
            }
            Integer status = sysObj.getStatus();
            if (Objects.nonNull(status) && 0 == status){//虚拟拨打(此处的值写死)
                Map falseMap = new HashMap();
                falseMap.put("id", request.getTaskId());//呼叫流程id
                falseMap.put("resultTaskId", "62925452-cbcc-490c-acfd-7e6994a69d68");
                falseMap.put("taskId", "603881051");
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(falseMap);
            }
            //    233 汉天呼叫中心
            if (Objects.equals(CaseFollowupRecord.CallType.HANTIAN.getValue().toString(), sysParam.getValue())) {
                if (Objects.isNull(user.getCallPhone())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User does not bind the main call number", "用户未绑定主叫号码")).body(null);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("command","callbackP2P");
                jsonObject.put("callerNum",request.getCaller()); //主叫号码
                jsonObject.put("calleeNum",request.getCallee()); //被叫号码
                jsonObject.put("callerShowNum",request.getCaller());
                jsonObject.put("calleeShowNum",request.getCallee());
                jsonObject.put("token",hantianToken);
                jsonObject.put("ext_field", UUID.randomUUID().toString());
                Map<String, Object> vars = new HashMap<>();
                vars.put("json",jsonObject.toJSONString());

                String callbackP2PUrl = callengine + "?json={json}";
                RestTemplate hantianRest = new RestTemplate();
                String result = hantianRest.getForObject(callbackP2PUrl,String.class,vars);
                JSONObject resultJson = JSONObject.parseObject(result);
                if(resultJson.getIntValue("code") ==  0){
                    return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(null);
                }else {
                    logger.error("汉天呼叫系统呼叫失败:" + resultJson.toJSONString());
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
                }
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Unknown system parameters of the call center", "未知呼叫中心的系统参数")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure", "操作失败")).body(null);
        }
    }

}
