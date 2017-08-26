package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.TemplateRepository;
import cn.fintecher.pangolin.entity.QTemplate;
import cn.fintecher.pangolin.entity.Template;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * Created by luqiang on 2017/7/24.
 */
@RestController
@RequestMapping("/api/templateController")
@Api(value = "TemplateController", description = "模板信息操作")
public class TemplateController extends BaseController {
    @Inject
    TemplateRepository templateRepository;
    @Autowired
    DataDictController dataDictController;
    private final Logger logger = LoggerFactory.getLogger(TemplateController.class);
    private static final String ENTITY_TEMPLATE = "template";

    @GetMapping("/getTemplateStyle")
    @ApiOperation(value = "查询模板形式", notes = "查询模板形式")
    public ResponseEntity getTemplateStyle() {
        return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "查询模板形式成功成功")).body(dataDictController.getDataDictByTypeCode("0009").getBody());
    }

    @GetMapping("/getTemplateType")
    @ApiOperation(value = "查询模板类别", notes = "查询模板类别")
    public ResponseEntity getTemplateType() {
        return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "查询模板类别成功")).body(dataDictController.getDataDictByTypeCode("0010").getBody());
    }

    /**
     * 模板查询
     */
    @GetMapping("getTemplatesByCondition")
    @ApiOperation(value = "模板按条件分页查询", notes = "模板按条件分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getTemplatesByCondition(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                  @QuerydslPredicate(root = Template.class)Predicate predicate,
                                                  @RequestHeader(value = "X-UserToken") String token,@ApiIgnore Pageable pageable) {
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            User user = getUserByToken(token);
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("templateDataModel", "TemplateDataModel", "请选择公司")).body(null);
                }
                builder.and(QTemplate.template.companyCode.eq(companyCode));
            }else{
                builder.and(QTemplate.template.companyCode.eq(user.getCompanyCode()));
            }
            Page<Template> page = templateRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/templateController/getTemplatesByCondition");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }

    }

    /**
     * 新增模板
     */
    @PostMapping("/createTemplate")
    @ApiOperation(value = "新增模板信息", notes = "新增模板信息")
    public ResponseEntity createTemplate(@Validated @RequestBody Template template, @RequestHeader(value = "X-UserToken") String token) {
        try {
            if (template.getIsDefault() && template.getTemplateStatus() == Status.Disable.getValue()) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "默认模板不可停用")).body(null);
            }
            User user = getUserByToken(token);
            if(Objects.isNull(user.getCompanyCode())){//如果是超级管理员，code码为空
                template.setCompanyCode(null);
            }else{
                template.setCompanyCode(user.getCompanyCode());
            }
            template.setCreator(user.getUserName());
            List<Template> templateList = templateRepository.findByTemplateNameOrTemplateCode(template.getTemplateName().trim(), template.getTemplateCode().trim());
            if (ZWStringUtils.isNotEmpty(templateList)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "该模板名称和编号已被占用")).body(null);
            }
            Template t = addTemplate(template);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "新增模块信息成功成功")).body(t);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }

    }

    /**
     * 更新模板信息
     *
     * @param template
     * @param token
     * @return
     */
    @PutMapping("/updateTemplate")
    @ApiOperation(value = "更新模板信息", notes = "更新模板信息")
    public ResponseEntity updateTemplate(@Validated @RequestBody Template template, @RequestHeader(value = "X-UserToken") String token) {
        try {
            Template existTemplate = templateRepository.findOne(template.getId());
            if (ZWStringUtils.isEmpty(existTemplate)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", "该模板已被删除")).body(null);
            }
            User user = getUserByToken(token);
            template.setCreator(user.getUserName());
            Template result = updateTemplate(template);
            if (result == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", "默认模板不可停用、取消默认、更改类别")).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("更新模板信息成功","template")).body(result);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getTemplateById")
    @ApiOperation(value = "根据模板ID查询模板", notes = "根据模板ID查询模板")
    public ResponseEntity getTemplateById(@RequestParam(required = true) @ApiParam("模板ID") String id) {
        try {
            Template template = templateRepository.findOne(id);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询模板信息成功","template")).body(template);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    /**
     * 删除模板
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteTemplateById")
    @ApiOperation(value = "根据模板ID删除模板", notes = "根据模板ID删除模板")
    public ResponseEntity deleteTemplateById(@RequestParam(required = true) @ApiParam("模板ID") String id) {
        try {
            Template template = templateRepository.findOne(id);
            if (template.getIsDefault()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", "默认模板不可删除")).body(null);
            }
            templateRepository.delete(id);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功","template")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getTemplateByNameOrCode")
    @ApiOperation(value = "判断新增模板名称、编号是否可用", notes = "判断新增模板名称、编号是否可用")
    public ResponseEntity getTemplateByNameOrCode(@RequestParam(required = false) @ApiParam("模板名称") String name,
                                                  @RequestParam(required = false) @ApiParam("模板编号") String code) {
        try {
            List<Template> templateNames = templateRepository.findByTemplateNameOrTemplateCode(name + "", "");
            if (!templateNames.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", "该名称已存在")).body(null);
            }
            List<Template> templateCodes = templateRepository.findByTemplateNameOrTemplateCode("", code + "");
            if (!templateCodes.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", "该编号已存在")).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert(ENTITY_TEMPLATE, "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "user", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getTemplatesByStyleAndType")
    @ApiOperation(value = "根据模板形式、类别、名称查询启用的模板", notes = "根据模板形式、类别、名称查询启用的模板")
    //在电催页面发送短信的时候会用到
    public ResponseEntity getTemplatesByStyleAndType(@RequestParam(required = false) @ApiParam("模板形式") Integer style, @RequestParam(required = false) @ApiParam("模板类别") Integer type,
                                                     @RequestParam(required = false) @ApiParam("模板名称") String name) {
        try {
            List<Template> list = templateRepository.findTemplatesByTemplateStyleAndTemplateTypeAndTemplateName(style, type, name);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询模板形式成功成功","ENTITY_TEMPLATE")).body(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_TEMPLATE, "template", e.getMessage())).body(null);
        }
    }

    /**
     * @param template 模板对象
     * @return 增加的模板对象
     * @author luqiang
     * @apiNote 增加模板方法
     */
    public Template addTemplate(Template template) {
        Integer style = template.getTemplateStyle();
        Integer type = template.getTemplateType();
        List<Template> listTemplates = templateRepository.findTemplatesByTemplateStyleAndTemplateType(style, type);
        //该形式、类别原来没有数据，则新建的设为默认模板
        if (listTemplates.isEmpty() && template.getTemplateStatus() == Status.Enable.getValue()) {
            template.setIsDefault(Template.DEFAULT_YES);
        }
        List<Template> temps = findDefaultTemplate(style, type);
        if (!listTemplates.isEmpty() && template.getIsDefault()) {
            Template t = temps.get(0);
            t.setIsDefault(Template.DEFAULT_NO);
            t.setUpdateTime(ZWDateUtil.getNowDateTime());
            templateRepository.save(t);
        }
        template.setCreateTime(ZWDateUtil.getNowDateTime());
        template.setUpdateTime(ZWDateUtil.getNowDateTime());
        return templateRepository.save(template);
    }

    /**
     * @param style 模板形式
     * @param type  模板类别
     * @author luqiang
     * @apiNote 根据模板形式、类别查询默认模板
     */
    public List<Template> findDefaultTemplate(Integer style, Integer type) {
        return templateRepository.findTemplatesByTemplateStyleAndTemplateTypeAndTemplateStatusAndIsDefault(style, type, Status.Enable.getValue(), Template.DEFAULT_YES);
    }

    public Template updateTemplate(Template template) {
        Template temp = templateRepository.findOne(template.getId());
        Integer nowType = template.getTemplateType();
        Boolean isDefault = temp.getIsDefault();
        Integer originalType = temp.getTemplateType();
        //修改前是默认模板的不可停用、不可取消默认、不可更改类别
        if (isDefault && (template.getTemplateStatus() == Status.Disable.getValue() || !template.getIsDefault() || nowType != originalType)) {
            return null;
        }
        if (template.getIsDefault()) {
            List<Template> listTemplates = findDefaultTemplate(template.getTemplateStyle(), template.getTemplateType());
            if (!listTemplates.isEmpty()) {
                Template tem = listTemplates.get(0);
                tem.setIsDefault(Template.DEFAULT_NO);
                tem.setUpdateTime(ZWDateUtil.getNowDateTime());
                templateRepository.save(tem);
            }
        }
        template.setUpdateTime(ZWDateUtil.getNowDateTime());
        template.setCreateTime(temp.getCreateTime());
        return templateRepository.save(template);
    }
}
