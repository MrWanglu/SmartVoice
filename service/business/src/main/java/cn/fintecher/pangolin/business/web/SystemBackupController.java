package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.SystemBackupRepository;
import cn.fintecher.pangolin.entity.SystemBackup;
import cn.fintecher.pangolin.entity.User;
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
import java.io.InputStreamReader;

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

    /**
     * @Description : 新增系统数据库备份
     */
    @PostMapping("/createSystemBackup")
    @ApiOperation(value = "增加系统数据库备份", notes = "增加系统数据库备份")
    public ResponseEntity<SystemBackup> createSystemBackup(@RequestBody SystemBackup request,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        request = (SystemBackup) EntityUtil.emptyValueToNull(request);
        logger.debug("REST request to save caseInfo : {}", request);
        if (request.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "新增不应该含有ID")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //调用shell脚本备份数据库
        try {
            logger.info("开始备份");
            String shpath="/data/mysqlscript/mysqlbackup.sh";
            Process ps = Runtime.getRuntime().exec(shpath);
            ps.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println(result);
            logger.info("备份返回值"+result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    //增加系统数据库备份
        SystemBackup systemBackup = systemBackupRepository.save(request);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(systemBackup);
    }
}
