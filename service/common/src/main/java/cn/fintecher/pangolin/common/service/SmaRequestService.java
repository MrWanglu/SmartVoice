package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.util.RandomUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;


@Service
public class SmaRequestService {

    private final Logger logger = LoggerFactory.getLogger(SmaRequestService.class);

    @Value("${pangolin.sma.seed}")
    private String seed;

    @Value("${pangolin.sma.smaUrl}")
    private String smaUrl;


    public ResponseEntity<Map<String, String>> smaRequest(String url, Map reqMap) {
        RestTemplate restTemplate = new RestTemplate();
        //组装请求头信息
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Authorization", createSign());
        HttpEntity<Object> httpEntity = new HttpEntity<>(reqMap, headers);
        ResponseEntity<String> entity = restTemplate.exchange(smaUrl + url, HttpMethod.POST, httpEntity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        if (entity.getStatusCode().is2xxSuccessful()) {
            try {
                Map<String, String> map = mapper.readValue(entity.getBody(), Map.class);
                if (Objects.equals(map.get("responseCode"), "1")) {
                    return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert("", "Is binding", "已经绑定")).body(map);
                }
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "error", map.get("responseinfo"))).body(null);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "error", "无法解析外呼平台返回")).body(null);
            }

        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "error", "外呼平台返回错误")).body(null);
        }
    }

    private String createSign() {
        net.minidev.json.JSONObject json = new net.minidev.json.JSONObject();
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
