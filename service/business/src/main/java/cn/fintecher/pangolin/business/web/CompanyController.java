package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.QCompany;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-03-14:22
 */
@RestController
@RequestMapping("/api/companyController")
@Api(value = "注册公司的信息管理", description = "注册公司的信息管理")
public class CompanyController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(CompanyController.class);
    @Autowired
    private CompanyRepository companyRepository;
    private static final String ENTITY_NAME = "Company";

    /**
     * @Description : 新增注册公司
     */
    @PostMapping("/createCompany")
    @ApiOperation(value = "新增注册公司", notes = "新增注册公司")
    public ResponseEntity<Company> createCompany(@Validated @ApiParam("公司对象") @RequestBody Company company,
                                                 @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        logger.debug("REST request to save company : {}", company);
        if (company.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "新增不应该含有ID")).body(null);
        }
        Company result = companyRepository.save(company);
        return ResponseEntity.created(new URI("/api/companyController/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId()))
                .body(result);
    }

    /**
     * @Description : 更新注册公司
     */
    @PostMapping("/updateCompany")
    @ApiOperation(value = "更新注册公司", notes = "更新注册公司")
    public ResponseEntity<Company> updateCompany(@Validated @ApiParam("公司对象") @RequestBody Company company,
                                                 @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        logger.debug("REST request to update company : {}", company);
        if (company.getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "修改应该含有ID")).body(null);
        }
        Company result = companyRepository.save(company);
        return ResponseEntity.created(new URI("/api/companyController/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId()))
                .body(result);
    }

    /**
     * @Description : 删除注册公司
     */
    @DeleteMapping("/company/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable String id) {
        logger.debug("REST request to delete caseInfo : {}", id);
        companyRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }

    /**
     * @Description : 查询注册公司
     */

    @PostMapping("/queryCompany")
    @ApiOperation(value = "查询注册公司", notes = "查询注册公司")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Company>> queryCompany(@RequestParam(required = false) String chinaName,
                                                      @RequestParam(required = false) String engName,
                                                      @RequestParam(required = false) Integer state,
                                                      @RequestParam(required = false) String code,
                                                      @RequestParam(required = false) String legPerson,
                                                      @RequestParam(required = false) String address,
                                                      @RequestParam(required = false) String city,
                                                      @RequestParam(required = false) String phone,
                                                      @RequestParam(required = false) String fax,
                                                      @RequestParam(required = false) String contactPerson,
                                                      @ApiIgnore Pageable pageable,
                                                      @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QCompany qCompany = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(chinaName)) {
            builder.and(qCompany.chinaName.like(chinaName.concat("%")));
        }
        if (Objects.nonNull(engName)) {
            builder.and(qCompany.engName.like(engName.concat("%")));
        }
        if (Objects.nonNull(state)) {
            builder.and(qCompany.status.eq(state));
        }
        if (Objects.nonNull(code)) {
            builder.and(qCompany.code.like(code.concat("%")));
        }
        if (Objects.nonNull(legPerson)) {
            builder.and(qCompany.legPerson.like(legPerson.concat("%")));
        }
        if (Objects.nonNull(address)) {
            builder.and(qCompany.address.like(address.concat("%")));
        }
        if (Objects.nonNull(city)) {
            builder.and(qCompany.city.like(city.concat("%")));
        }
        if (Objects.nonNull(phone)) {
            builder.and(qCompany.phone.like(phone.concat("%")));
        }
        if (Objects.nonNull(fax)) {
            builder.and(qCompany.fax.like(fax.concat("%")));
        }
        if (Objects.nonNull(contactPerson)) {
            builder.and(qCompany.contactPerson.like(contactPerson.concat("%")));
        }
        Page<Company> page = companyRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 获取所有公司
     */
    @GetMapping(value = "/getAllCompany")
    @ApiOperation(value = "获取所有公司", notes = "获取所有公司")
    public ResponseEntity<List<Company>> getAllCompany() {
        List<Company> list = companyRepository.findAll();
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(list);
    }
}
