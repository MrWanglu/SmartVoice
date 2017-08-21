package cn.fintecher.pangolin.common.web.rest;


import cn.fintecher.pangolin.common.model.SmaErpv3Return;
import cn.fintecher.pangolin.common.service.CallService;
import cn.fintecher.pangolin.common.service.SmaRequestService;
import cn.fintecher.pangolin.entity.message.AddTaskVoiceFileMessage;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/smaResource")
@ApiIgnore
public class SmaResource {
    private final Logger log = LoggerFactory.getLogger(SmaResource.class);
    @Autowired
    SmaRequestService smaRequestService;
    @Autowired
    private CallService callService;
    @Autowired
    RestTemplate restTemplate;
    //中通天鸿的录音下载
    @Value("${pangolin.zhongtong-server.callout}")
    private String callout;


    /**
     * @Description : erpv3 163
     */
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
        SmaErpv3Return smaErpv3Return = smaRequestService.smaRequest("addTaskVoiceFileBytaskId.html", paramMap);
        if (Objects.equals("success",smaErpv3Return.getSign())) {
            Map<String, String> resultMap = smaErpv3Return.getMap();
            log.info("{} 获取录音成功:{}", request.getRecorderId(), resultMap);
            return ResponseEntity.ok().body(resultMap.get("taskVoiceFileUrl"));
        } else {
            log.info("{} {}", request.getRecorderId());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    /**
     * @Description : 中通天鸿 164 调用接口保存录音到本地
     */
    @GetMapping("/getRecordingByCallId")
    @ApiOperation(value = "调用接口保存录音到本地", notes = "调用接口保存录音到本地")
    public ResponseEntity<String> getRecordingByCallId(@RequestParam String callId) {
        try {
            HttpClient client = new HttpClient();
            client.setConnectionTimeout(1000 * 60);
            client.getHostConfiguration().setHost(callout, 80, "http");
            HttpMethod method = callService.downloadRecord(callId);
            client.executeMethod(method);
            System.out.println("*****" + method.getStatusLine());
             /* getResponseBodyAsStream start */
            InputStream inputStream = method.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer response = new StringBuffer();
            String read = "";
            while ((read = br.readLine()) != null) {
                response.append(read);
            }
            System.out.println(response);
            /* getResponseBodyAsStream end */
            String url = response.substring(20, (response.length() - 2)); //截取文件下载地址
            url = url.replaceAll("\\\\", "");
            System.out.println(url);
            //下载录音并保存在本地
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            RestTemplate restTemplate1 = new RestTemplate();
            ResponseEntity<byte[]> response1 = restTemplate1.exchange(url, org.springframework.http.HttpMethod.GET, entity, byte[].class);
            String filePath = FileUtils.getTempDirectoryPath().concat("record.mp3");
            FileOutputStream output = new FileOutputStream(new File(filePath));
            IOUtils.write(response1.getBody(), output);
            IOUtils.closeQuietly(output);
            FileSystemResource resource = new FileSystemResource(new File(filePath));
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            url = restTemplate.postForObject("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            return ResponseEntity.ok().body(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().body("fail");
        }
    }
}

