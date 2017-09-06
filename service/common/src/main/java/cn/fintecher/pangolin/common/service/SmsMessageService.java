package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.common.model.SMSMessage;
import cn.fintecher.pangolin.common.respository.SMSMessageRepository;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.message.PaaSMessage;
import cn.fintecher.pangolin.entity.util.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by Administrator on 2017/3/24.
 */
@Service("smsMessageService")
public class SmsMessageService {
    private final Logger log = LoggerFactory.getLogger(SmsMessageService.class);
    //获取配置文件中的值
    @Value("${pangolin.message.url}")
    private String messageUrl;
    @Value("${pangolin.message.channel}")
    private String channel;
    @Value("${pangolin.message.sysNumber}")
    private String sysNumber;
    @Value("${pangolin.message.seed}")
    private String seed;
    @Value("${pangolin.message.verificationCode}")
    private String verificationCode;
    //极光配置
    @Value("${pangolin.jiguang.appKey}")
    private String appKey;
    @Value("${pangolin.jiguang.masterSecret}")
    private String masterSecret;
    @Value("${pangolin.jiguang.msgUrl}")
    private String msgUrl;
    //PaaS变量短息
    @Value("${pangolin.smsVariable.account}")
    private String account;
    @Value("${pangolin.smsVariable.pswd}")
    private String pswd;
    @Value("${pangolin.smsVariable.smsVariableUrl}")
    private String smsVariableUrl;

    @Autowired
    SMSMessageRepository smsMessageRepository;
    @Autowired
    RestTemplate restTemplate;

    /**
     * 发送短信
     *
     * @param
     * @return 返回发送结果
     */
    public void sendMessage(SMSMessage message) {

        String result;
        try {
            Map<String, Object> reqMap = new LinkedHashMap<>();
            reqMap.put("mobile", message.getPhoneNumber());
            reqMap.put("number", message.getTemplate());
            reqMap.put("sysNumber", sysNumber);
            message.getParams().put("channel", channel);
            message.getParams().put("verification_code", verificationCode);
            reqMap.put("params", message.getParams());
            //组装请求头信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept-Charset", "UTF-8");
            headers.set("Authorization", createSign());
            HttpEntity<Object> httpEntity = new HttpEntity<>(reqMap, headers);
            ResponseEntity entity = new RestTemplate().exchange(messageUrl, HttpMethod.POST, httpEntity, String.class);
            result = entity.getBody().toString();
            log.debug(result);
            smsMessageRepository.save(message);
            return;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
    }

    public String sendMessageJiGuang(SMSMessage message) {
        ResponseEntity entity = null;
        try {
            //组装请求头信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept-Charset", "UTF-8");
            String authorization = appKey.concat(":").concat(masterSecret);
            authorization = cn.fintecher.pangolin.entity.util.Base64.encodeJava8(authorization, "UTF-8");
            headers.set("Authorization", "Basic ".concat(authorization));
            Map<String, Object> reqMap = new LinkedHashMap<>();
            reqMap.put("mobile", message.getPhoneNumber());
            reqMap.put("temp_id", message.getTemplate());
            reqMap.put("temp_para", message.getParams());
            HttpEntity<Object> httpEntity = new HttpEntity<>(reqMap, headers);
            log.info("极光发送短信信息body {} header {}", reqMap, headers);
            entity = new RestTemplate().exchange(msgUrl, HttpMethod.POST, httpEntity, String.class);
            log.info("极光发送短信信息回执 {}", entity.getBody());
            smsMessageRepository.save(message);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return message.getPhoneNumber();
        }
    }

    /**
     * 数字签名
     */
    private String createSign() {
        JSONObject json = new JSONObject();
        String timestamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
        String accountId = RandomUtil.getRandomNumber(20);
        json.put("timestamp", timestamp);
        //目前 随机生成 后期分模块拓展
        json.put("accountId", accountId);
        String[] array = new String[]{seed, timestamp, accountId};
        StringBuilder sb = new StringBuilder();
        // 字符串排序
        Arrays.sort(array);
        for (int i = 0; i < 3; i++) {
            sb.append(array[i]);
        }
        String str = sb.toString();
        json.put("sign", DigestUtils.sha1Hex(str));
        return json.toJSONString();
    }

    public String sendMessagePaaS(PaaSMessage message) {
        try {
            ResponseEntity entity = null;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept-Charset", "UTF-8");
            JSONObject object = new JSONObject();
            object.put("account", account);
            object.put("password", pswd);
            object.put("phone", message.getPhoneNumber());
            object.put("report", true);
            object.put("msg", message.getContent());
            HttpEntity<Object> httpEntity = new HttpEntity<>(object, headers);
            log.info("云通讯发送短信信息body {} header {}", object, headers);
            entity = new RestTemplate().exchange(smsVariableUrl, HttpMethod.POST, httpEntity, String.class);
            log.info("云通讯发送短信信息回执 {}", entity.getBody());
            JSONObject jsonObject = JSONObject.parseObject(entity.getBody().toString());
            if (Objects.equals(jsonObject.get("code"), "0")) {
                return null;
            }
            return message.getPhoneNumber();
        } catch (Exception e) {
            return message.getPhoneNumber();
        }
    }
}


