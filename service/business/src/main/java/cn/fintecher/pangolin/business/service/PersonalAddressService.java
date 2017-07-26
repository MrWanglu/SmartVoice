package cn.fintecher.pangolin.business.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/21.
 */
@Service("PersonalAddressService")
public class PersonalAddressService {
    @Value("${cuibei.baiduMap.webUrl}")
    private String webUrl;
    @Value("${cuibei.baiduMap.webAk}")
    private String webAk;

    /**
     * @return
     * @throws Exception
     */
    @Cacheable(value = "mapCache", key = "'mapstore:address:'+#address", unless = "#result==null")
    public List<Double> getAddLngLat(String address) {
        List<Double> list = new ArrayList<Double>();
        String url = String.format(webUrl.concat(webAk).concat("&output=json&q=%s"), address);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        if (Objects.nonNull(entity)) {
            String body = entity.getBody();
            if (body.contains("location")) {
                JSONObject jsonObject = JSON.parseObject(entity.getBody());
                String msg = jsonObject.getString("message");
                if (StringUtils.isNotBlank(msg) && msg.equals("ok")) {
                    String results = jsonObject.getString("results");
                    if (StringUtils.isNotBlank(results)) {
                        if (Pattern.matches("^\\[.*\\]$", results.trim())) {
                            JSONArray jsonArray = JSON.parseArray(results);
                            if (!jsonArray.isEmpty()) {
                                results = jsonArray.get(0).toString();
                                if (StringUtils.isNotBlank(results)) {
                                    JSONObject jsonObject1 = JSONObject.parseObject(results);
                                    JSONObject jsonObject2 = JSONObject.parseObject(jsonObject1.getString("location"));
                                    list.add(Double.valueOf(jsonObject2.getString("lng")));
                                    list.add(Double.valueOf(jsonObject2.getString("lat")));
                                } else {
                                    throw new RuntimeException("地址信息无效");
                                }
                            } else {
                                throw new RuntimeException("地址信息无效");
                            }
                        } else {
                            throw new RuntimeException("地址信息无效");
                        }
                    } else {
                        throw new RuntimeException("地址信息无效");
                    }
                } else {
                    throw new RuntimeException("获取地址信息失败");
                }
            } else {
                throw new RuntimeException("地址信息无效");
            }
        } else {
            throw new RuntimeException("获取地址信息失败");
        }
        return list;
    }
}


