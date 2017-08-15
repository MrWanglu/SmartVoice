package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.OutSourceCommssionList;
import cn.fintecher.pangolin.entity.Role;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-15-12:01
 */
@RestController
@RequestMapping("/api/outSourceCommssionController")
@Api(value = "委外方佣金管理", description = "委外方佣金管理")
public class OutSourceCommssionController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(OutSourceCommssionController.class);
    private static final String ENTITY_NAME = "OutSource";

    /**
     * @Description : 新增/修改委外佣金
     */
    @PostMapping("/createOutSourceCommssion")
    @ApiOperation(value = "新增/修改委外佣金", notes = "新增/修改委外佣金")
    public ResponseEntity<Role> createOutSourceCommssion(@RequestBody OutSourceCommssionList request) {
        if (Objects.isNull(request.getOutsourceCommissionList())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Add new or modified objects", "请添加新增或者修改的对象")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }
}