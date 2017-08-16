package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.OutSourceCommissionIds;
import cn.fintecher.pangolin.business.model.OutSourceCommssionList;
import cn.fintecher.pangolin.business.repository.OutSourceCommssionRepository;
import cn.fintecher.pangolin.entity.OutSourceCommssion;
import cn.fintecher.pangolin.entity.QOutSourceCommssion;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    @Autowired
    OutSourceCommssionRepository outSourceCommssionRepository;

    /**
     * @Description : 根据委外方id和公司code码查询委外佣金
     */
    @GetMapping(value = "/getOutSourceCommission")
    @ApiOperation(value = "根据委外方id和公司code码查询委外佣金", notes = "根据委外方id和公司code码查询委外佣金")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<OutSourceCommssion>> getOutSourceCommission(@RequestParam String outsId,
                                                                           @RequestParam String companyCode,
                                                                           @ApiIgnore Pageable pageable) {
        log.debug("REST request to get a page of AccOutsource : {}");
        if (Objects.isNull(outsId)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Please select the foreign party", "请选择委外方")).body(null);
        }
        QOutSourceCommssion qOutSourceCommssion = QOutSourceCommssion.outSourceCommssion;
        Page<OutSourceCommssion> page = outSourceCommssionRepository.findAll(qOutSourceCommssion.outsource.id.eq(outsId).and(qOutSourceCommssion.companyCode.eq(companyCode)), pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新增/修改委外佣金
     */
    @PostMapping("/createOutSourceCommssion")
    @ApiOperation(value = "新增/修改委外佣金", notes = "新增/修改委外佣金")
    public ResponseEntity<List<OutSourceCommssion>> createOutSourceCommssion(@RequestBody OutSourceCommssionList request) {
        if (Objects.isNull(request.getOutsourceCommissionList())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Add new or modified objects", "请添加新增或者修改的对象")).body(null);
        }
        List<OutSourceCommssion> exist = new ArrayList<>();
        for (OutSourceCommssion outSourceCommssion : request.getOutsourceCommissionList()) {
            QOutSourceCommssion qOutSourceCommssion = QOutSourceCommssion.outSourceCommssion;
            Iterator<OutSourceCommssion> outSourceCommssionList;
            if (Objects.nonNull(outSourceCommssion.getId())) {
                outSourceCommssionList = outSourceCommssionRepository.findAll(qOutSourceCommssion.outsource.id.eq(outSourceCommssion.getOutsource().getId()).and(qOutSourceCommssion.overdueTime.eq(outSourceCommssion.getOverdueTime())).and(qOutSourceCommssion.id.ne(outSourceCommssion.getId())).and(qOutSourceCommssion.companyCode.eq(outSourceCommssion.getCompanyCode()))).iterator();
            } else {
                outSourceCommssionList = outSourceCommssionRepository.findAll(qOutSourceCommssion.outsource.id.eq(outSourceCommssion.getOutsource().getId()).and(qOutSourceCommssion.overdueTime.eq(outSourceCommssion.getOverdueTime())).and(qOutSourceCommssion.companyCode.eq(outSourceCommssion.getCompanyCode()))).iterator();
            }
            List<OutSourceCommssion> outSourceCommssionList1 = IteratorUtils.toList(outSourceCommssionList);
            if (outSourceCommssionList1.size() == 0) {
                outSourceCommssionRepository.save(outSourceCommssion);
            } else {
                exist.add(outSourceCommssion);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(exist);
    }

    /**
     * * @Description : 删除委外佣金
     */
    @PostMapping("/deleteOutsourceCommission")
    @ApiOperation(value = "删除委外佣金", notes = "删除委外佣金")
    public ResponseEntity<String> addAccOutsourceCommission(@RequestBody @ApiParam("委外佣金id集合") OutSourceCommissionIds request) {
        for (String id : request.getIds()) {
            OutSourceCommssion outSourceCommssion = outSourceCommssionRepository.findOne(id);
            if (Objects.nonNull(outSourceCommssion)) {
                outSourceCommssionRepository.delete(id);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }
}