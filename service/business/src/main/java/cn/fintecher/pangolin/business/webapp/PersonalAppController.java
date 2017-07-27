package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.web.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.inject.Inject;

/**
 * @author : gaobeibei
 * @Description : APP个人信息
 * @Date : 11:28 2017/7/27
 */

@RestController
@RequestMapping(value = "/api/PersonalAppController")
@Api(value = "APP客户信息", description = "APP客户信息")
public class PersonalAppController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(PersonalAppController.class);

    @Inject
    PersonalRepository personalRepository;


    @GetMapping("/getPersonalForApp")
    @ApiOperation(value = "根据客户ID获取客户信息", notes = "根据客户ID获取客户信息")
    public ResponseEntity<Personal> getPersonal(@RequestParam @ApiParam(value = "客户ID", required = true) String id) {
        log.debug("REST request to get personal : {}", id);
        Personal personal = personalRepository.findOne(id);
        return ResponseEntity.ok().body(personal);
    }
}
