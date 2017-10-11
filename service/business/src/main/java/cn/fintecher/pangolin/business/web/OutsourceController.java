package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.OutDistributeInfo;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.business.repository.OutsourceRepository;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.*;

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
    private static final String ENTITY_NAME = "OutSource";
    @Autowired
    private OutsourceRepository outsourceRepository;
    @Autowired
    private BatchSeqService batchSeqService;
    @Autowired
    private OutsourcePoolRepository outsourcePoolRepository;

    /**
     * @Description : 新增/修改委外方管理
     */
    @PostMapping("/createOutsource")
    @ApiOperation(value = "新增/修改委外方管理", notes = "新增/修改委外方管理")
    public ResponseEntity<Outsource> createOutsource(@RequestBody Outsource outsource,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        outsource = (Outsource) EntityUtil.emptyValueToNull(outsource);
        log.debug("REST request to save department : {}", outsource);
        if (Objects.isNull(outsource.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The company logo cannot be empty", "公司标识不能为空")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(outsource.getId())) {
            //验证委外方是否重名
            QOutsource qOutsource = QOutsource.outsource;
            Iterator<Outsource> outsourceIterator = outsourceRepository.findAll(qOutsource.outsName.eq(outsource.getOutsName()).and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode())).and(qOutsource.companyCode.eq(outsource.getCompanyCode()))).iterator();
            if (outsourceIterator.hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The outsourcename is not allowed to be used", "该名称已被占用，请重新输入")).body(null);
            }
            LabelValue labelValue = batchSeqService.nextSeq(Constants.PRIN_SEQ, Outsource.principalStatus.PRINCODE_DIGIT.getPrincipalCode());
            if (Objects.isNull(labelValue)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The client code for failure", "委托方编号获取失败")).body(null);
            }
            String code = labelValue.getValue();
            String letter;
            switch (outsource.getOutsOrgtype()) {
                case 155:
                    letter = "P";
                    break;
                case 156:
                    letter = "B";
                    break;
                case 157:
                    letter = "I";
                    break;
                default:
                    letter = "O";
                    break;
            }
            String subCode = code.substring(1);
            //委外方编码
            outsource.setOutsCode(letter + subCode);
            //启用状态0
            outsource.setFlag(Outsource.deleteStatus.START.getDeleteCode());
            outsource.setOperateTime(ZWDateUtil.getNowDateTime()); //创建时间
            outsource.setUser(user);
            Outsource outsourceReturn = outsourceRepository.save(outsource);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(outsourceReturn);
        } else {
            Outsource outsource1 = outsourceRepository.findOne(outsource.getId());
            //验证委外方是否重名
            QOutsource qOutsource = QOutsource.outsource;
            Iterator<Outsource> outsourceIterator = outsourceRepository.findAll(qOutsource.outsName.eq(outsource.getOutsName()).and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode())).and(qOutsource.id.ne(outsource.getId())).and(qOutsource.companyCode.eq(outsource.getCompanyCode()))).iterator();
            if (outsourceIterator.hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The outsourcename is not allowed to be used", "该名字不允许被使用")).body(null);
            }
            BeanUtils.copyProperties(outsource, outsource1);
            Outsource outsource2 = outsourceRepository.save(outsource1);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取成功", "")).body(outsource2);
        }
    }

    /**
     * @Description : 查询委外方
     */
    @GetMapping("/query")
    @ApiOperation(value = "查询委外方", notes = "查询委外方")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Outsource>> query(@RequestParam(required = false) String companyCode,
                                                 @RequestParam(required = false) String outsCode,
                                                 @RequestParam(required = false) String outsName,
                                                 @RequestParam(required = false) String outsAddress,
                                                 @RequestParam(required = false) String outsContacts,
                                                 @RequestParam(required = false) String outsPhone,
                                                 @RequestParam(required = false) String outsMobile,
                                                 @RequestParam(required = false) String outsEmail,
                                                 @RequestParam(required = false) String creator,
                                                 @RequestParam(required = false) Integer outsOrgtype,
                                                 @ApiIgnore Pageable pageable,
                                                 @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QOutsource qOutsource = QOutsource.outsource;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.isNull(user.getCompanyCode())) {//超级管理员默认查所有记录
            if (Objects.nonNull(companyCode)) {
                builder.and(qOutsource.companyCode.eq(companyCode));
            }
        } else {
            builder.and(qOutsource.companyCode.eq(user.getCompanyCode()));
        }
        if (Objects.nonNull(outsCode)) {
            builder.and(qOutsource.outsCode.like(outsCode.concat("%")));
        }
        if (Objects.nonNull(outsName)) {
            builder.and(qOutsource.outsName.like(outsName.concat("%")));
        }
        if (Objects.nonNull(outsAddress)) {
            builder.and(qOutsource.outsAddress.like(outsAddress.concat("%")));
        }
        if (Objects.nonNull(outsContacts)) {
            builder.and(qOutsource.outsContacts.like(outsContacts.concat("%")));
        }
        if (Objects.nonNull(outsPhone)) {
            builder.and(qOutsource.outsPhone.like(outsPhone.concat("%")));
        }
        if (Objects.nonNull(outsMobile)) {
            builder.and(qOutsource.outsMobile.like(outsMobile.concat("%")));
        }
        if (Objects.nonNull(outsEmail)) {
            builder.and(qOutsource.outsEmail.like(outsEmail.concat("%")));
        }
        if (Objects.nonNull(creator)) {
            builder.and(qOutsource.user.userName.like(creator.concat("%")));
        }
        if (Objects.nonNull(outsOrgtype)) {
            builder.and(qOutsource.outsOrgtype.eq(outsOrgtype));
        }
        builder.and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode()));
        Page<Outsource> page = outsourceRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(page);
    }

    /**
     * @Description : 删除委外方
     */
    @DeleteMapping("/deleteOutsource")
    @ApiOperation(value = "删除委外方", notes = "删除委外方")
    public ResponseEntity<Outsource> deleteOutsource(@RequestParam String id,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
        Iterator<OutsourcePool> outsourcePoolIterator = outsourcePoolRepository.findAll(qOutsourcePool.outsource.id.eq(id)).iterator();
        if (outsourcePoolIterator.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The client's association case is not allowed to be deleted", "该委外方关联案件不允许删除")).body(null);
        }
        Outsource outsource = outsourceRepository.findOne(id);
        outsource.setFlag(Outsource.deleteStatus.BLOCK.getDeleteCode());
        Outsource outsource1 = outsourceRepository.save(outsource);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(null);
    }

    /**
     * @Description : 查询所有委外方
     */
    @GetMapping("/getAllOutsource")
    @ApiOperation(value = "查询所有委外方", notes = "查询所有委外方")
    public ResponseEntity<List<Outsource>> getAllOutsource(@RequestParam(required = false) String companyCode,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QOutsource qOutsource = QOutsource.outsource;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(user.getCompanyCode())) {//超级管理员默認查所有记录
            builder.and(qOutsource.companyCode.eq(user.getCompanyCode()));
        } else {
            if (Objects.nonNull(companyCode)) {
                builder.and(qOutsource.companyCode.eq(companyCode));
            }
        }
        builder.and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode()));
        Iterator<Outsource> outsourceIterator = outsourceRepository.findAll(builder).iterator();
        List<Outsource> outsourceList = IteratorUtils.toList(outsourceIterator);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(outsourceList);
    }

    /**
     * @Description : 统计委托方信息的 案件信息
     */
    @GetMapping("/getAllOutSourceInfoByCase")
    @ApiOperation(value = "统计委托方信息的 案件信息 ", notes = "统计委托方信息的 案件信息 ")
    public ResponseEntity<List<OutDistributeInfo>> getAllOutSourceInfoByCase(@RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        try {

            QOutsource qOutsource = QOutsource.outsource;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.nonNull(user.getCompanyCode())) {//超级管理员默認查所有记录
                builder.and(qOutsource.companyCode.eq(user.getCompanyCode()));
            }
            builder.and(qOutsource.flag.eq(Outsource.deleteStatus.START.getDeleteCode()));
            Iterable<Outsource> outsourceIterator = outsourceRepository.findAll(builder);
            Set<String> outIds = new HashSet<>();
            for (Outsource outsource : outsourceIterator) {
                outIds.add(outsource.getId());
            }
            List<OutDistributeInfo> outDistributeInfos = new ArrayList<>();
            if (!outIds.isEmpty()) {
                Object[] objects = outsourcePoolRepository.getAllOutSourceByCase(outIds);
                for (int i = 0; i < objects.length; i++) {
                    Object[] object1 = (Object[]) objects[i];
                    if (Objects.nonNull(object1[1])) {
                        OutDistributeInfo outDistributeInfo = new OutDistributeInfo();
                        outDistributeInfo.setOutName(Objects.isNull(object1[0]) ? null : object1[0].toString());
                        outDistributeInfo.setOutCode(Objects.isNull(object1[1]) ? null : object1[1].toString());
                        outDistributeInfo.setCaseCount(Objects.isNull(object1[2]) ? null : Integer.parseInt(object1[2].toString()));
                        outDistributeInfo.setEndCount(Objects.isNull(object1[3]) ? null : Integer.parseInt(object1[3].toString()));
                        outDistributeInfo.setSuccessRate(Objects.isNull(object1[4]) ? null : BigDecimal.valueOf(Double.valueOf(object1[4].toString())));
                        outDistributeInfo.setCaseAmt(Objects.isNull(object1[5]) ? null : BigDecimal.valueOf(Double.valueOf(object1[5].toString())));
                        outDistributeInfos.add(outDistributeInfo);
                    }
                }
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "")).body(outDistributeInfos);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "获取委外方信息失败")).body(null);
        }
    }
}
