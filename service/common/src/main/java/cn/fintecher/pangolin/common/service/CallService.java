package cn.fintecher.pangolin.common.service;

import cn.fintecher.pangolin.common.model.AddTaskRecorderRequest;
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
    //erpv3系统参数
    @Value("${pangolin.call-server.enterprise-code}")
    private String enterpriseCode;
    @Value("${pangolin.call-server.two-way-interface}")
    private String twoWayInterface;
    @Value("${pangolin.call-server.secret}")
    private String secret;
    //中通天鸿系统参数  联系人  师秋艳 QQ 2853152686
    private static final String userName = "4216052701";  //公司的唯一标识  中通天鸿开通的
    private static final String proceedSign = "59715406"; //中继号  目前这个是唯一的
    private static final String timeout = "50";
    @Value("${pangolin.zhongtong-server.cti}")
    private String cti;
    @Value("${pangolin.zhongtong-server.webCall1800}")
    private String webCall1800;
    //中通天鸿的电话呼叫
    public String encode(String nonce, String created, String secret) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        String decode = decryptBASE64(nonce) + created + secret;
        return Base64.encode(decode.getBytes());
    }

    public String decryptBASE64(String key) {
        return new String(Base64.decode(key));
    }

//    //超时时间的设置
//    public ClientHttpRequestFactory simpleClientHttpRequestFactory(String host, int port) {
//        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setReadTimeout(15000);// ms
//        factory.setConnectTimeout(15000);// ms
//        //// 设置代理服务器地址和端口
//        SocketAddress address = new InetSocketAddress(host, port);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
//        factory.setProxy(proxy);
//        return factory;
//    }

    public void tianHongCallUp(AddTaskRecorderRequest request) {
//        HttpClient client = new HttpClient();
//        client.setConnectionTimeout(1000 * 60);
//        client.getHostConfiguration().setHost(cti, 80, "http");
//        HttpMethod method ;
//        PostMethod post = new PostMethod(webCall1800);
//        client.executeMethod(method);
//        String Nonce = new BASE64Encoder().encode("123456abc".getBytes()); //随机字符串
//        String Created = String.valueOf(System.currentTimeMillis());
//        String PasswordDigest = "";
//        try {
//            PasswordDigest = encode(Nonce, Created, secret);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        String headerValue = "UsernameToken Username=\"" + userName + "\",PasswordDigest=\"" + PasswordDigest + "\",Nonce=\"" + Nonce + "\",Created=\"" + Created + "\"";
//        HttpHeaders requestHeaders = new HttpHeaders();
////        http请求头的键名（大小写无关）固定字符串，后面添加一个空格
//        requestHeaders.add("X-WSSE", headerValue);
////        NameValuePair model1 = new NameValuePair("caller", request.getCaller());
////        NameValuePair model2 = new NameValuePair("called", request.getCallee());
////        NameValuePair model3 = new NameValuePair("timeout", timeout);
////        NameValuePair model4 = new NameValuePair("display_caller", proceedSign);
////        NameValuePair model5 = new NameValuePair("display_called", proceedSign);
////        NameValuePair[] params = new NameValuePair[]{model1, model2, model3, model4, model5};
//        Map map = new HashMap();
//        map.put("caller", request.getCaller());
//        map.put("called", request.getCallee());
//        map.put("timeout", timeout);
//        map.put("display_caller", proceedSign);
//        map.put("display_called", proceedSign);
//        RestTemplate result = restTemplate.postForObject(cti,"text/xml","text/xml",map, requestHeaders);
//         restTemplate.exchange(cti, org.springframework.http.HttpMethod.POST,map);

//        2.     HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
//        ResponseEntity<String> response = template.exchange(url, HttpMethod.GET, requestEntity, String.class, params);
//        String sttr = response.getBody();
//        HttpMethod method = callService.getPostMethod(request);
//        client.executeMethod(method);

////            System.out.println(method.getStatusLine());
//
//            /* getResponseBodyAsStream start */
//        InputStream inputStream = method.getResponseBodyAsStream();
//        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//        StringBuffer response = new StringBuffer();
//        String read = "";
//        while ((read = br.readLine()) != null) {
//            response.append(read);
//        }
////            System.out.println(response);
//        /* getResponseBodyAsStream start */
//        method.releaseConnection();

    }


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


}
