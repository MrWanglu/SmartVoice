package cn.fintecher.pangolin.common.service;

import cn.fintecher.pangolin.common.model.CallRequest;
import cn.fintecher.pangolin.entity.util.Base64;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-06-21-17:31
 */
@Service
public class CallService {

    private final Logger log = LoggerFactory.getLogger(CallService.class);
    @Value("${pangolin.call-server.enterprise-code}")
    private String enterpriseCode;
    @Value("${pangolin.call-server.two-way-interface}")
    private String twoWayInterface;
    @Value("${pangolin.call-server.secret}")
    private String secret;

//    //中通天鸿的电话呼叫
//    public void tianHongCallUp(AddTaskRecorderRequest request) {
//        HttpClient client = new HttpClient();
//        client.setConnectionTimeout(1000 * 60);
//        client.getHostConfiguration().setHost(callCti, 80, "http");
//    }


    public HttpMethod getPostMethod(CallRequest request) {
        PostMethod post = new PostMethod(twoWayInterface);
        String timeValue = Long.toString(System.currentTimeMillis());
        String Nonce = timeValue;   //  随机字符串，引号为必需的，后续再添加一个英文逗号
        String Created = timeValue;  //当前请求的时间的时间戳，引号为必需的
        String PasswordDigest = "";
        try {
            PasswordDigest = encode(Nonce, Created, secret);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String headerValue = "UsernameToken Username=\"" + enterpriseCode + "\",PasswordDigest=\"" + PasswordDigest + "\",Nonce=\"" + Nonce + "\",Created=\"" + Created + "\"";
        post.setRequestHeader("X-WSSE", headerValue);   //http请求头的键名（大小写无关）固定字符串，后面添加一个空格
//        String test = "{\"filter\":{\"start_time\":\"2014-05-06 00:00:00\"}}";
        NameValuePair model1 = new NameValuePair("caller", request.getCaller());
        NameValuePair model2 = new NameValuePair("called", request.getCalled());
        NameValuePair model3 = new NameValuePair("timeout", request.getTimeout());
        NameValuePair model4 = new NameValuePair("display_caller", request.getDisplayCaller());
        NameValuePair model5 = new NameValuePair("display_called", request.getDisplayCalled());
        post.setRequestBody(new NameValuePair[]{model1, model2, model3, model4, model5});
        return post;
    }

    public String encode(String nonce, String created, String secret) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        String decode = decryptBASE64(nonce) + created + secret;
        return Base64.encode(decode.getBytes());
    }

    public String decryptBASE64(String key) {
        return new String(Base64.decode(key));
    }
}
