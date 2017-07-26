package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.OutsourceRepository;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.Outsource;
import cn.fintecher.pangolin.entity.QOutsource;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-10:14
 */
@RestController
@RequestMapping("/api/outsourceController")
@Api(value = "委外方管理", description = "委外方管理")
public class OutsourceController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(OutsourceController.class);
    @Autowired
    private OutsourceRepository outsourceRepository;
    private static final String ENTITY_NAME = "OutSource";

    /**
     * @Description : 新增/修改委外方管理
     */
    @PostMapping("/createForeignManage")
    @ApiOperation(value = "新增/修改委外方管理", notes = "新增/修改委外方管理")
    public ResponseEntity<Department> createForeignManage(@RequestBody Outsource outsource,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save department : {}", outsource);

        if (outsource.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增委外方不应该含有ID")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //验证委外方是否重名
        QOutsource qOutsource = QOutsource.outsource;
        Iterator<Outsource> outsourceIterator = outsourceRepository.findAll(qOutsource.outsName.eq(outsource.getOutsName()).and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode()))).iterator();
        if (outsourceIterator.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The outsourcename is not allowed to be used", "该名字不允许被使用")).body(null);
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增委外方不应该含有ID")).body(null);
    }
}
