package cn.fintecher.pangolin.common.scheduled;

import cn.fintecher.pangolin.entity.util.Constants;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-11:36
 */
@Component
@EnableScheduling
@Lazy(value = false)
public class RecordDayScheduled {
    private final Logger log = LoggerFactory.getLogger(RecordDayScheduled.class);

    //云翳参数配置
    @Value("${pangolin.yunyi-server.host}")
    private String host;
    @Value("${pangolin.yunyi-server.port}")
    private int port;
    @Value("${pangolin.yunyi-server.timeout}")
    private int timeout;

    @Scheduled(cron = "0 59 23 * * ?")
    void callHeartBeat() throws IOException {
        log.info("发送心跳" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            Map<String,String> map= Constants.map;
            for(String value : map.values()){
                Socket socket = new Socket(host, port);
                socket.setSoTimeout(timeout);
                BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                String sendData = "<request><cmdType>heartbeat</cmdType><agentID>"+value+"</agentID></request>";
                String sendDataUtf82 = new String(sendData.getBytes("UTF-8"), "UTF-8");
                String head2 = "<<<length=" + sendDataUtf82.getBytes("UTF-8").length + ">>>";
                sendDataUtf82 = head2 + sendDataUtf82;
                System.out.println("心跳：" + sendDataUtf82);
                pw.print(sendDataUtf82);
                pw.flush();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Scheduled(cron = "* 0/30 0/23 0/1 * ?")
    void resetMap() throws IOException {
        log.info("清除Map" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            Map<String,String> map= Constants.map;
            map.clear();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
