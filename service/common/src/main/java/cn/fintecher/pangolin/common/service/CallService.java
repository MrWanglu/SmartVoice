package cn.fintecher.pangolin.common.service;

import cn.fintecher.pangolin.common.model.AddTaskRecorderRequest;
import cn.fintecher.pangolin.entity.util.Constants;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-06-21-17:31
 */
@Service
public class CallService {
    private final Logger log = LoggerFactory.getLogger(CallService.class);
    //erpv3系统参数
    @Value("${pangolin.call-server.two-way-interface}")
    private String twoWayInterface;
    @Value("${pangolin.call-server.secret}")
    private String secret;
    //中通天鸿系统参数  联系人  师秋艳 QQ 2853152686
    private static final String timeout = "50";
    @Value("${pangolin.zhongtong-server.enterprise-code}")
    private String enterpriseCode;
    @Value("${pangolin.zhongtong-server.proceedSign}")
    private String proceedSign;
    @Value("${pangolin.zhongtong-server.cti}")
    private String cti;
    @Value("${pangolin.zhongtong-server.webCall1800}")
    private String webCall1800;
    @Value("${pangolin.zhongtong-server.secret}")
    private String ztSecret;
    //中通天鸿下载录音
    @Value("${pangolin.zhongtong-server.downloadrRecord}")
    private String downloadrRecord;

    //中通天鸿的电话呼叫
    public String encode(String nonce, String created, String secret) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        String decode = decryptBASE64(nonce) + created + secret;
        return new BASE64Encoder().encode(md.digest(decode.getBytes()));
    }

    public String decryptBASE64(String key) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            return new String(decoder.decodeBuffer(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @Description : 中通天鸿 164 中通天鸿的打电话接口
     */

    public HttpMethod getPostMethod(AddTaskRecorderRequest request) {
        PostMethod post = new PostMethod(cti);
        String Nonce = new BASE64Encoder().encode("123456abc".getBytes()); //随机字符串
        String Created = String.valueOf(System.currentTimeMillis());
        String PasswordDigest = "";
        try {
            PasswordDigest = encode(Nonce, Created, ztSecret);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String headerValue = "UsernameToken Username=\"" + enterpriseCode + "\",PasswordDigest=\"" + PasswordDigest + "\",Nonce=\"" + Nonce + "\",Created=\"" + Created + "\"";
        post.setRequestHeader("X-WSSE", headerValue);   //http请求头的键名（大小写无关）固定字符串，后面添加一个空格
//        String test = "{\"filter\":{\"start_time\":\"2014-05-06 00:00:00\"}}";
        NameValuePair model1 = new NameValuePair("caller", request.getCaller());
        NameValuePair model2 = new NameValuePair("called", request.getCallee());
        NameValuePair model3 = new NameValuePair("timeout", timeout);
        NameValuePair model4 = new NameValuePair("display_caller", proceedSign);
        NameValuePair model5 = new NameValuePair("display_called", proceedSign);
        post.setRequestBody(new NameValuePair[]{model1, model2, model3, model4, model5});
        return post;
    }

    /**
     * @Description : 中通天鸿 164 下载保存录音
     */
    public HttpMethod downloadRecord(String callId) {
        String agId = null;
        String params = "vcc_code=" + enterpriseCode + "&call_id=" + callId + "&result_type=1" + "&ag_id=" + agId;

        GetMethod get = new GetMethod(downloadrRecord + "?" + params);
        String Nonce = new BASE64Encoder().encode("123456abc".getBytes()); //随机字符串
        String Created = String.valueOf(System.currentTimeMillis());
        String PasswordDigest = "";
        try {
            PasswordDigest = encode(Nonce, Created, secret);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String headerValue = "UsernameToken Username=\"" + enterpriseCode + "\",PasswordDigest=\"" + PasswordDigest + "\",Nonce=\"" + Nonce + "\",Created=\"" + Created + "\"";
        get.setRequestHeader("X-WSSE", headerValue); //http请求头的键名（大小写无关）固定字符串，后面添加一个空格
        return get;
    }

    /**
     * @Description : 云羿 165 打电话的签入动作
     */
    public Map<String, String> signIn(String key, String value) {
        Map<String, String> map = Constants.map;
        map.put(key, value);
        return map;
    }

    /**
     * @Description : 云羿 165 打电话的签出动作
     */
    public Map<String, String> signOut(String key, String value) {
        Map<String, String> map = Constants.map;
        map.remove(key);
        return map;
    }
}
