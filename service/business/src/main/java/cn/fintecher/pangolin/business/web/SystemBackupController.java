package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.SystemBackupRepository;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.SystemBackup;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-09-21-9:21
 */
@RestController
@RequestMapping("/api/systemBackupController")
@Api(value = "SystemBackupController", description = "系统数据库备份")
public class SystemBackupController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(SystemBackupController.class);
    private static final String ENTITY_NAME = "SystemBackup";

    @Autowired
    private SystemBackupRepository systemBackupRepository;
    @Autowired
    private SysParamRepository sysParamRepository;

    /**
     * @Description : 新增系统数据库备份
     */
    @PostMapping("/createSystemBackup")
    @ApiOperation(value = "增加系统数据库备份", notes = "增加系统数据库备份")
    public ResponseEntity<SystemBackup> createSystemBackup(@RequestBody SystemBackup request,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        request = (SystemBackup) EntityUtil.emptyValueToNull(request);
        logger.debug("REST request to save caseInfo : {}", request);
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(request.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "error for companycode", "公司code码不能为空")).body(null);
        }
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParams = null;
        try {
            sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.MYSQL_BACKUP_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MYSQL_BACKUP_ADDRESS_TYPE)).and(qSysParam.companyCode.eq(request.getCompanyCode())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "系统数据库备份地址参数异常")).body(null);
        }
        //调用shell脚本备份mysql数据库
        BufferedReader br = null;
        Process ps = null;
        String result = null;
        try {
            logger.info("开始备份");
            String[] shpath = {sysParams.getValue(), user.getUserName()};
            ps = Runtime.getRuntime().exec(shpath);
            ps.waitFor();
            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            result = sb.toString();
            Pattern p = Pattern.compile(".*sql");
            Matcher m = p.matcher(result);
            while (!(m.find())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Mysql database backup failed", "mysql数据库备份失败")).body(null);
            }
//            System.out.println(result);
            logger.info("备份返回值" + result);
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
        request.setMysqlName(result);
        //增加系统数据库备份
        SystemBackup systemBackup = systemBackupRepository.save(request);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(systemBackup);
    }

    /**
     * @Description : 恢复系统数据库备份
     */
    @PostMapping("/recoverSystemBackup")
    @ApiOperation(value = "恢复系统数据库备份", notes = "恢复系统数据库备份")
    public ResponseEntity<String> recoverSystemBackup(@RequestBody SystemBackup request,
                                                      @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save caseInfo : {}", request);
        if (Objects.isNull(request.getMysqlName()) && Objects.isNull(request.getMongdbName())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Missing database file", "数据库文件缺失")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParams = null;
        try {
            sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.MYSQL_RECOVER_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MYSQL_RECOVER_ADDRESS_TYPE)).and(qSysParam.companyCode.eq(request.getCompanyCode())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "恢复系统数据库地址参数异常")).body(null);
        }
        //调用shell脚本备份数据库
        BufferedReader br = null;
        Process ps = null;
        String result = null;
        try {
            logger.info("开始恢复备份");
            String[] shpath = {sysParams.getValue(), request.getMysqlName()};
            ps = Runtime.getRuntime().exec(shpath);
            ps.waitFor();
            br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
//            System.out.println(result);
            logger.info("恢复备份返回值" + result);
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
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(result);
    }
}
