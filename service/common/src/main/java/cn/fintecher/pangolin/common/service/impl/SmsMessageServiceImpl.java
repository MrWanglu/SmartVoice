package cn.fintecher.pangolin.common.service.impl;

import cn.fintecher.pangolin.common.model.SMSMessage;
import cn.fintecher.pangolin.common.respository.SMSMessageRepository;
import cn.fintecher.pangolin.common.service.SmsMessageService;
import cn.fintecher.pangolin.entity.message.SendSMSMessage;
import cn.fintecher.pangolin.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Administrator on 2017/3/24.
 */

@Service("smsMessageService")
public class SmsMessageServiceImpl implements SmsMessageService {


    private final Logger log = LoggerFactory.getLogger(SmsMessageServiceImpl.class);
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

    @Autowired
    private SMSMessageRepository repository;

    @Override
    public void sendMessage(SendSMSMessage message) {
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
            SMSMessage smsMessage = new SMSMessage();
            BeanUtils.copyProperties(message, smsMessage);
            log.debug(result);
            repository.save(smsMessage);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
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
