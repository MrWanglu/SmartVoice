package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.common.model.SMSMessage;
import cn.fintecher.pangolin.common.respository.SMSMessageRepository;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.*;
import net.minidev.json.JSONObject;
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
    @Value("${cuibei.message.url}")
    private String messageUrl;
    @Value("${cuibei.message.channel}")
    private String channel;
    @Value("${cuibei.message.sysNumber}")
    private String sysNumber;
    @Value("${cuibei.message.seed}")
    private String seed;
    @Value("${cuibei.message.verificationCode}")
    private String verificationCode;
    //极光配置
    @Value("${cuibei.jiguang.appKey}")
    private String appKey;
    @Value("${cuibei.jiguang.masterSecret}")
    private String masterSecret;
    @Value("${cuibei.jiguang.msgUrl}")
    private String msgUrl;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    SMSMessageRepository smsMessageRepository;

    /**
     * 发送短信
     *
     * @param
     * @return 返回发送结果
     */
    public void sendMessage(SMSMessage message) {
        //短信配置
        SysParam sysParam = restTemplate.getForEntity("http://business-service/api/sysParamResource?userId=" + message.getUserId() + "&companyCode=" + message.getCompanyCode() + "&code=" + Constants.SMS_PUSH_CODE + "&type=" + Constants.SMS_PUSH_TYPE, SysParam.class).getBody();
        // 0 erpv3 1 极光
        if (Objects.equals(sysParam.getValue(), "0")) {
            RestTemplate restTemplate = new RestTemplate();
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
                ResponseEntity entity = restTemplate.exchange(messageUrl, HttpMethod.POST, httpEntity, String.class);
                result = entity.getBody().toString();
                log.debug(result);
               smsMessageRepository.save(message);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        } else if (Objects.equals(sysParam.getValue(), "1")) {
            sendMessageJiGuang(message);
        }
    }
    public void sendMessageJiGuang(SMSMessage message) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> entity = null;
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
            entity = restTemplate.exchange(msgUrl, HttpMethod.POST, httpEntity, String.class);
            log.info("极光发送短信信息回执 {}", entity.getBody());
            smsMessageRepository.save(message);
        } catch (Exception e) {
            e.printStackTrace();
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
}


