package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.DeleteSystemBackupIds;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.SystemBackupRepository;
import cn.fintecher.pangolin.business.service.SystemBackupService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.Iterator;
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
    @Autowired
    private SystemBackupService systemBackupService;

    /**
     * @Description : 查询系统备份
     */

    @GetMapping("/querySystemBackup")
    @ApiOperation(value = "查询系统备份", notes = "查询系统备份")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<SystemBackup>> querySystemBackup(@RequestParam(required = false) String companyCode,
                                                                @RequestParam(required = false) Integer type,
                                                                @RequestParam(required = false) String mysqlName,
                                                                @RequestParam(required = false) String mongdbName,
                                                                @RequestParam(required = false) String operator,
                                                                @RequestParam(required = false) Date operateTime,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QSystemBackup qSystemBackup = QSystemBackup.systemBackup;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qSystemBackup.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(type)) {
            builder.and(qSystemBackup.type.eq(type));
        }
        if (Objects.nonNull(mysqlName)) {
            builder.and(qSystemBackup.mysqlName.like(mysqlName.concat("%")));
        }
        if (Objects.nonNull(mongdbName)) {
            builder.and(qSystemBackup.mongdbName.like(mongdbName.concat("%")));
        }
        if (Objects.nonNull(operator)) {
            builder.and(qSystemBackup.operator.like(operator.concat("%")));
        }
        Page<SystemBackup> page = systemBackupRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(page);
    }

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
        //mysql数据库备份
        QSysParam qSysParam = QSysParam.sysParam;
        Iterator<SysParam> mysqlSysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.MYSQL_BACKUP_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MYSQL_BACKUP_ADDRESS_TYPE))).iterator();
        if (!mysqlSysParams.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "mysql系统数据库备份地址参数异常")).body(null);
        }
        String result = systemBackupService.operationShell(mysqlSysParams.next().getValue(), user.getUserName());
        Pattern p = Pattern.compile(".*sql");
        Matcher m = p.matcher(result);
        while (!(m.find())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Mysql database backup failed", "mysql数据库备份失败")).body(null);
        }
        //mongodb数据库备份
        Iterator<SysParam> mongodbSysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.MONGODB_BACKUP_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MONGODB_BACKUP_ADDRESS_TYPE))).iterator();
        if (!mongodbSysParams.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "mongodb系统数据库备份地址参数异常")).body(null);
        }
        String mongodbResult = systemBackupService.operationShell(mongodbSysParams.next().getValue(), user.getUserName());
        Pattern mongodbp = Pattern.compile(".*mongodb");
        Matcher mongodbm = mongodbp.matcher(mongodbResult);
        while (!(mongodbm.find())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "mongodb database backup failed", "mongodb数据库备份失败")).body(null);
        }
        request.setType(1);
        request.setMysqlName(result);
        request.setMongdbName(mongodbResult);
        request.setOperator(user.getRealName());
        request.setOperateTime(ZWDateUtil.getNowDateTime());
        //增加系统数据库备份
        SystemBackup systemBackup = systemBackupRepository.save(request);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(systemBackup);
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
        //mysql数据库恢复
        Iterator<SysParam> mysqlSysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.MYSQL_RECOVER_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MYSQL_RECOVER_ADDRESS_TYPE))).iterator();
        if (!mysqlSysParams.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "恢复mysql系统数据库地址参数异常")).body(null);
        }
        String mysqlResult = systemBackupService.operationShell(mysqlSysParams.next().getValue(), request.getMysqlName());
        if (!Objects.equals("success", mysqlResult)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail to recover mysql database", "mysql数据库恢复失败")).body(null);
        }
        //mongodb数据库恢复
        Iterator<SysParam> mongodbSysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.MONGODB_RECOVER_ADDRESS_CODE).and(qSysParam.type.eq(Constants.MONGODB_RECOVER_ADDRESS_TYPE))).iterator();
        if (!mongodbSysParams.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exception for parameters", "恢复mongodb系统数据库地址参数异常")).body(null);
        }
        String mongodbResult = systemBackupService.operationShell(mongodbSysParams.next().getValue(), request.getMongdbName());
        if (!Objects.equals("success", mongodbResult)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail to recover mongodb database", "mongodb数据库恢复失败")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body("mysql"+mysqlResult+"mongodb"+mongodbResult);
    }

    /**
     * @Description : 删除系统数据库备份
     */
    @PostMapping("/deleteSystemBackup")
    @ApiOperation(value = "删除系统数据库备份", notes = "删除系统数据库备份")
    public ResponseEntity<String> deleteSystemBackup(@RequestBody DeleteSystemBackupIds request,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        for (String id : request.getIds()) {
            systemBackupRepository.delete(id);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(null);
    }
}
