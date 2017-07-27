package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-27-13:48
 */
@RestController
@RequestMapping("/api/sysParamResource")
public class SysParamResource {
    private static final String ENTITY_NAME = "sysParamResource";
    @Autowired
    UserRepository userRepository;
    @Autowired
    private SysParamRepository sysParamRepository;

    /**
     * @Description : 通过TypeCode查找系统参数
     */
    @GetMapping
    @ApiOperation(value = "通过TypeCode查找系统参数", notes = "通通过TypeCode查找系统参数")
    public ResponseEntity<SysParam> getSysParamByCodeAndType(@RequestParam String userId,
                                                             @RequestParam String companyCode,
                                                             @RequestParam String code,
                                                             @RequestParam String type) {
        QSysParam qSysParam = QSysParam.sysParam;
        //超级管理员的系统参数
        Iterator<SysParam> sysParams;
        if (Objects.equals(Constants.ADMIN_DEPARTMENT_ID, userId)) {
            sysParams = sysParamRepository.findAll(qSysParam.code.eq(code).and(qSysParam.type.eq(type)).and(qSysParam.companyCode.isNull())).iterator();
            if (sysParams.hasNext()) {
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(sysParams.next());
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail to get", "获取失败")).body(null);
            }
        }
        //普通的管理员的系统参数 普通管理员必须传 companyCode
        sysParams = sysParamRepository.findAll(qSysParam.code.eq(code).and(qSysParam.type.eq(type)).and(qSysParam.companyCode.eq(companyCode))).iterator();
        if (sysParams.hasNext()) {
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(sysParams.next());
        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail to get", "获取失败")).body(null);
        }
    }
}
