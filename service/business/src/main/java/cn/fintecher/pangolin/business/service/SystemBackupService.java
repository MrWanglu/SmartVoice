package cn.fintecher.pangolin.business.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-09-22-9:36
 */
@Service("SystemBackupService")
public class SystemBackupService {
    final Logger logger = LoggerFactory.getLogger(SystemBackupService.class);
    /**
     * @Description : 操作shell脚本完成mysql数据库备份
     */
    public String operationShell(String sysParamsValue,String mysqlName){
        //调用shell脚本备份mysql数据库
        BufferedReader br = null;
        Process ps = null;
        String result = null;
        try {
            logger.info("开始mysql备份");
            String[] shpath = {sysParamsValue, mysqlName};
            ps = Runtime.getRuntime().exec(shpath);
            ps.waitFor();
            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            logger.info("mysql数据库备份返回值" + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (ps != null) {
                    ps.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
