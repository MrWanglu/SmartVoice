package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.PersonalInfoExportModel;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/personalController")
@Api(value = "PersonalController", description = "客户信息操作")
public class PersonalController extends BaseController{

    private static final String ENTITY_NAME = "personal";
    private final Logger log = LoggerFactory.getLogger(PersonalController.class);

    @Inject
    private PersonalRepository personalRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;

    @PostMapping("/personalInfoExport")
    @ApiOperation(value = "客户信息导出", notes = "客户信息导出")
    public ResponseEntity personalInfoExport(@RequestBody @ApiParam("配置项") PersonalInfoExportModel model) {
        Integer exportType = model.getExportType();  //导出维度  0-催收员，1-产品类型，2-批次号，3-案件状态
        Set<Object> dataFilter = model.getDataFilter(); // 数据过滤
        Map<String, List<String>> dataInfo = model.getDataInfo(); //数据项

        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        // 催收员
        if (Objects.equals(exportType, 0)) {
            List<String> orgData = dataInfo.get("orgData"); // 数据信息
            BooleanExpression exp = qCaseInfo.currentCollector.realName.in((CollectionExpression<?, ? extends String>) dataFilter);
            Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
            // TODO 导出
            Map<String,String> headMap = new HashMap<>();
            headMap.put("","");
            Map<String,Object> dataMap = new HashMap<>();
            if (!orgData.isEmpty()) {
                orgData.forEach(e-> dataMap.put(e,""));
            }

        }

        // 产品类型
        if (Objects.equals(exportType, 1)) {
            List<String> baseInfo = dataInfo.get("baseInfo"); // 基本信息
            List<String> workInfo = dataInfo.get("workInfo"); // 工作信息
            List<String> contactInfo = dataInfo.get("contactInfo"); // 联系人信息
            List<String> bankInfo = dataInfo.get("bankInfo"); // 开户信息
            BooleanExpression exp = qCaseInfo.product.productSeries.seriesName.in((CollectionExpression<?, ? extends String>) dataFilter);
            Iterable<CaseInfo> all = caseInfoRepository.findAll(exp);
            // TODO 导出
        }

        // 批次号
        if (Objects.equals(exportType, 2)) {

        }

        // 案件状态
        if (Objects.equals(exportType, 3)) {

        }

        return null;
    }

    @PostMapping("/personal")
    public ResponseEntity<Personal> createPersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to save personal : {}", personal);
        if (personal.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.created(new URI("/api/personal/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/personal")
    public ResponseEntity<Personal> updatePersonal(@RequestBody Personal personal) throws URISyntaxException {
        log.debug("REST request to update Personal : {}", personal);
        if (personal.getId() == null) {
            return createPersonal(personal);
        }
        Personal result = personalRepository.save(personal);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, personal.getId().toString()))
                .body(result);
    }

    @GetMapping("/personal")
    public List<Personal> getAllPersonal() {
        log.debug("REST request to get all Personal");
        List<Personal> personalList = personalRepository.findAll();
        return personalList;
    }

    @GetMapping("/queryPersonal")
    public ResponseEntity<Page<Personal>> queryPersonal(@QuerydslPredicate(root = Personal.class) Predicate predicate, @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get all Personal");

        Page<Personal> page = personalRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryPersonal");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/personal/{id}")
    public ResponseEntity<Personal> getPersonal(@PathVariable String id) {
        log.debug("REST request to get personal : {}", id);
        Personal personal = personalRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(personal));
    }

    @DeleteMapping("/personal/{id}")
    public ResponseEntity<Void> deletePersonal(@PathVariable String id) {
        log.debug("REST request to delete personal : {}", id);
        personalRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * @Description 费用减免审批页面多条件查询减免记录
     */
    @GetMapping("/getPersonalCaseInfo")
    @ApiOperation(value = "客户查询", notes = "客户查询（分页、条件）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<Page<CaseInfo>> getPersonalCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable) throws URISyntaxException {
        Page<CaseInfo> page = caseInfoRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accDerateController/getPersonalCaseInfo");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }
//    @PostMapping("/createExcelTemplate")
//    @ResponseBody
//    @ApiOperation(value = "客户信息导出", notes = "客户信息导出")
//    public ResponseEntity createExcelTemplate(@RequestBody @ApiParam("配置项") Map<String, Object> map) {
//
//
//        return null;
//    }

}
