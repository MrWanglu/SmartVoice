package cn.fintecher.pangolin.business.scheduled;

import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.SystemBackupRepository;
import cn.fintecher.pangolin.business.service.SystemBackupService;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.SystemBackup;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-11:36
 */
@Component
@EnableScheduling
@Lazy(value = false)
public class SystemBackupScheduled {
    private final Logger log = LoggerFactory.getLogger(SystemBackupScheduled.class);

    @Autowired
    private SystemBackupRepository systemBackupRepository;
    @Autowired
    private SystemBackupService systemBackupService;
    @Autowired
    private SysParamRepository sysParamRepository;

    @Scheduled(cron = "0 0 0/23 * * ?")
    void systemBackup() throws IOException {
        log.info("开始mysql数据库备份" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        try {
            SystemBackup request = new SystemBackup();
            QSysParam qSysParam = QSysParam.sysParam;
            Iterator<SysParam> sysParams = null;
            try {
                sysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.MYSQL_BACKUP_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MYSQL_BACKUP_ADDRESS_TYPE))).iterator();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sysParams.hasNext()) {
                String result = systemBackupService.operationShell(sysParams.next().getValue(), "Administrator");
                Pattern p = Pattern.compile(".*sql");
                Matcher m = p.matcher(result);
                while (m.find()) {
                    request.setType(0);
                    request.setMysqlName(result);
                    request.setOperator("Administrator");
                    request.setOperateTime(ZWDateUtil.getNowDateTime());
                    systemBackupRepository.save(request);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
