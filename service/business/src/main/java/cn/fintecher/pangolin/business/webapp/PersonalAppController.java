package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.AddressRepairInfo;
import cn.fintecher.pangolin.business.model.RepairInfoModel;
import cn.fintecher.pangolin.business.repository.PersonalAddressRepository;
import cn.fintecher.pangolin.business.repository.PersonalContactRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.util.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.List;

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

    @Inject
    PersonalAddressRepository personalAddressRepository;

    @Inject
    PersonalContactRepository personalContactRepository;

    @GetMapping("/getPersonalForApp")
    @ApiOperation(value = "查询客户信息for APP", notes = "查询客户信息for APP")
    public ResponseEntity<Personal> getPersonal(@RequestParam @ApiParam(value = "客户ID", required = true) String id) {
        log.debug("REST request to get personal : {}", id);
        Personal personal = personalRepository.findOne(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "Personal")).body(personal);
    }

    @GetMapping("/getContactInfoForApp")
    @ResponseBody
    @ApiOperation(value = "查询联系人信息for APP", notes = "查询联系人信息for APP")
    public ResponseEntity<List<PersonalContact>> getContactInfoForApp(@RequestParam @ApiParam(value = "客户ID", required = true) String id) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPersonalContact.personalContact.personalId.eq(id));
        List<PersonalContact> personalContactList = IterableUtils.toList(personalContactRepository.findAll(builder, new Sort(Sort.Direction.DESC, "source")));
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "PersonalContactList")).body(personalContactList);
    }

    @GetMapping("/getAddressInfoForApp")
    @ResponseBody
    @ApiOperation(value = "查询地址信息for APP", notes = "查询地址信息for APP")
    public ResponseEntity<List<PersonalAddress>> getAddressInfoForApp(@RequestParam @ApiParam(value = "客户ID", required = true) String id) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPersonalAddress.personalAddress.personalId.eq(id));
        List<PersonalAddress> personalAddressList = IterableUtils.toList(personalAddressRepository.findAll(builder, new Sort(Sort.Direction.DESC, "source")));
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "PersonalAddressList")).body(personalAddressList);
    }
}
