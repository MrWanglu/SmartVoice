package cn.fintecher.pangolin.business.webapp;


import cn.fintecher.pangolin.business.model.MapModel;
import cn.fintecher.pangolin.business.model.PersonalRepairInfo;
import cn.fintecher.pangolin.business.repository.CaseFileRepository;
import cn.fintecher.pangolin.business.repository.PersonalAddressRepository;
import cn.fintecher.pangolin.business.repository.PersonalContactRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.service.AccMapService;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author : gaobeibei
 * @Description : APP个人信息
 * @Date : 11:28 2017/7/27
 */

@RestController
@RequestMapping(value = "/api/personalAppController")
@Api(value = "APP客户信息", description = "APP客户信息")
public class PersonalAppController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(PersonalAppController.class);

    @Inject
    PersonalRepository personalRepository;

    @Inject
    PersonalAddressRepository personalAddressRepository;

    @Inject
    PersonalContactRepository personalContactRepository;

    @Inject
    CaseFileRepository caseFileRepository;

    @Inject
    AccMapService accMapService;

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
        List<PersonalContact> list = IterableUtils.toList(personalContactRepository.findAll(builder, new Sort(Sort.Direction.DESC, "source")));
        for (int i = 0 ;i < list.size() -  1 ;i ++ ){
            for( int j = list.size() -  1 ; j  >  i;j -- ){
                if(Objects.equals(list.get(j).getPhone(),list.get(i).getPhone())){
                    list.remove(j);
                }
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "PersonalContactList")).body(list);
    }

    @GetMapping("/getAddressInfoForApp")
    @ResponseBody
    @ApiOperation(value = "查询地址信息for APP", notes = "查询地址信息for APP")
    public ResponseEntity<List<PersonalAddress>> getAddressInfoForApp(@RequestParam @ApiParam(value = "客户ID", required = true) String id) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QPersonalAddress.personalAddress.personalId.eq(id));
        List<PersonalAddress> personalAddressList = IterableUtils.toList(personalAddressRepository.findAll(builder, new Sort(Sort.Direction.DESC, "source")));

        for(int i=0; i<personalAddressList.size(); i++){
            PersonalAddress personalAddress = personalAddressList.get(i);
            if (Objects.isNull(personalAddress.getLongitude())
                    || Objects.isNull(personalAddress.getLatitude())) {
                try{
                    MapModel model = accMapService.getAddLngLat(personalAddress.getDetail());
                    personalAddress.setLatitude(BigDecimal.valueOf(model.getLatitude()));
                    personalAddress.setLongitude(BigDecimal.valueOf(model.getLongitude()));
                }catch(Exception e1){
                    e1.getMessage();
                }
            }
        }
        personalAddressRepository.save(personalAddressList);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "PersonalAddressList")).body(personalAddressList);
    }

    /**
     * @Description 添加修复信息
     */
    @PostMapping("/saveRepairInfoForApp")
    @ApiOperation(value = "APP添加修复信息", notes = "APP添加修复信息")
    public ResponseEntity saveRepairInfo(@RequestBody PersonalRepairInfo personalRepairInfo,
                                         @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save repair information");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        if (Objects.isNull(personalRepairInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "", "修复内容为空")).body(null);
        }
        if (Objects.isNull(personalRepairInfo.getRelation()) || StringUtils.isBlank(personalRepairInfo.getName())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "", "关系/姓名为必填项")).body(null);
        }
        PersonalAddress personalAddress = new PersonalAddress();
        BeanUtils.copyProperties(personalRepairInfo, personalAddress);
        BeanUtils.copyProperties(personalRepairInfo.getAddressList().get(0),personalAddress);
        personalAddress.setSource(Constants.DataSource.REPAIR.getValue());
        personalAddress.setOperator(user.getId());
        personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalAddressRepository.saveAndFlush(personalAddress);
        for(int i = 0; i < personalRepairInfo.getSocialList().size(); i++){
            PersonalContact personalContact = new PersonalContact();
            BeanUtils.copyProperties(personalRepairInfo, personalContact);
            BeanUtils.copyProperties(personalRepairInfo.getPhoneList().get(0), personalContact);
            BeanUtils.copyProperties(personalRepairInfo.getSocialList().get(i),personalContact);
            personalContact.setSource(Constants.DataSource.REPAIR.getValue());
            personalContact.setOperator(user.getId());
            personalContact.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactRepository.saveAndFlush(personalContact);
        }
        List<CaseFile> caseFileList = new ArrayList<>();
        Iterator<String> iterator = personalRepairInfo.getFileIds().iterator();
            while(iterator.hasNext()){
            CaseFile caseFile = new CaseFile();
            caseFile.setCompanyCode(user.getCompanyCode());
            caseFile.setOperatorTime(ZWDateUtil.getNowDateTime());
            caseFile.setOperator(user.getId());
            caseFile.setFileId(iterator.next());
            caseFile.setCaseNumber(personalRepairInfo.getCaseNnumber());
            caseFileList.add(caseFile);
        }
        caseFileRepository.save(caseFileList);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("修复成功", "")).body(null);
    }
}
