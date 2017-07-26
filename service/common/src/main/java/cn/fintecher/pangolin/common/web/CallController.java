package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.model.CallRequest;
import cn.fintecher.pangolin.common.service.CallService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-06-21-17:16
 */
@RestController
@RequestMapping(value = "/api/CallController")
@Api(value = "呼叫相关接口", description = "呼叫相关接口")
public class CallController {
    private static final Logger logger = LoggerFactory.getLogger(CallController.class);

    @Autowired
    CallService callService;
    @Value("${pangolin.call-server.host}")
    private String host;
    @Value("${pangolin.call-server.port}")
    private String port;
    @Value("${pangolin.call-server.protocol}")
    private String protocol;

    @PostMapping("/call")
    @ApiOperation(value = "双向外呼", notes = "双向外呼")
    public ResponseEntity<Void> bindTaskDataByCallerId(@RequestBody CallRequest request) throws IOException {

        //POST http://m.icsoc.net/v2/wintelapi/webcall/cti
        //POST http://m.icsoc.net/v2/wintelapi/webcall/webcall800
        //POST http://m.icsoc.net/v2/wintelapi/webcall/webcall400
        HttpClient client = new HttpClient();
        client.getHostConfiguration().setHost(host, Integer.valueOf(port), protocol);
        HttpMethod method = callService.getPostMethod(request);
        client.executeMethod(method);
        System.out.println(method.getStatusLine());
        String response = new String(method.getResponseBodyAsString().getBytes("utf-8"));
        System.out.println(response);
        method.releaseConnection();
        return ResponseEntity.ok().body(null);

    }
}
