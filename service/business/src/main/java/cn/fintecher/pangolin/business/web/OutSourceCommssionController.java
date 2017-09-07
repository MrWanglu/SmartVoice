package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.OutSourceCommssionRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
    @Autowired
    RestTemplate restTemplate;

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
                                                                           @ApiIgnore Pageable pageable,
                                                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get a page of AccOutsource : {}");
        User user;
        BooleanBuilder builder = new BooleanBuilder();
        QOutSourceCommssion qOutSourceCommssion = QOutSourceCommssion.outSourceCommssion;
        try {
            user = getUserByToken(token);
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
                builder.and(qOutSourceCommssion.companyCode.eq(companyCode));
            } else {
                builder.and(qOutSourceCommssion.companyCode.eq(user.getCompanyCode()));;//限制公司code码
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }

        //判断传入的委外案件是否为空
        if (Objects.isNull(outsId)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Please select the foreign party", "请选择委外方")).body(null);
        }else{

            builder.and(qOutSourceCommssion.outsId.eq(outsId));
        }
        Page<OutSourceCommssion> page = outSourceCommssionRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新增/修改委外佣金
     * Updated by huyanmin 2017/9/4
     * Verified the user if log in
     */
    @PostMapping("/createOutSourceCommssion")
    @ApiOperation(value = "新增/修改委外佣金", notes = "新增/修改委外佣金")
    public ResponseEntity<List<OutSourceCommssion>> createOutSourceCommssion(@RequestBody OutSourceCommssionList request,
                                                                             @RequestHeader(value = "X-UserToken") String token) {
        User user;

        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }

        if (Objects.isNull(request.getOutsourceCommissionList())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Add new or modified objects", "请添加新增或者修改的对象")).body(null);
        }

        List<OutSourceCommssion> exist = new ArrayList<>();
        for (OutSourceCommssion outSourceCommssion : request.getOutsourceCommissionList()) {
            QOutSourceCommssion qOutSourceCommssion = QOutSourceCommssion.outSourceCommssion;
            Iterator<OutSourceCommssion> outSourceCommssionList;
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(outSourceCommssion.getCompanyCode())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
            } else {
                outSourceCommssion.setCompanyCode(user.getCompanyCode());//限制公司code码
            }
            //判断该佣金案件是否存在
            if (Objects.nonNull(outSourceCommssion.getId())) {
                outSourceCommssionList = outSourceCommssionRepository.findAll(qOutSourceCommssion.outsId.eq(outSourceCommssion.getOutsId()).and(qOutSourceCommssion.overdueTime.eq(outSourceCommssion.getOverdueTime())).and(qOutSourceCommssion.id.ne(outSourceCommssion.getId())).and(qOutSourceCommssion.companyCode.eq(outSourceCommssion.getCompanyCode()))).iterator();
            } else {
                //该逾期时段不能为空
                if(outSourceCommssion.getOverdueTime() !=null && !"".equals(outSourceCommssion.getOverdueTime())){
                    long list = outSourceCommssionRepository.count(qOutSourceCommssion.overdueTime.eq(outSourceCommssion.getOverdueTime()));
                    //该逾期时段已存在
                    if(list == 0){
                        outSourceCommssionList = outSourceCommssionRepository.findAll(qOutSourceCommssion.outsId.eq(outSourceCommssion.getOutsId()).and(qOutSourceCommssion.overdueTime.eq(outSourceCommssion.getOverdueTime())).and(qOutSourceCommssion.companyCode.eq(outSourceCommssion.getCompanyCode()))).iterator();
                    }else{
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failed", "该逾期时段已存在")).body(null);
                    }
                }else{
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failed", "该逾期时段不能为空")).body(null);
                }

            }
            List<OutSourceCommssion> outSourceCommssionList1 = IteratorUtils.toList(outSourceCommssionList);
            if (outSourceCommssionList1.size() == 0) {
                outSourceCommssion.setOperateTime(ZWDateUtil.getNowDateTime());
                outSourceCommssion.setOperator(user.getUserName());
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
    public ResponseEntity<String> deleteOutsourceCommission(@RequestBody @ApiParam("委外佣金id集合") OutSourceCommissionIds request,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        for (String id : request.getIds()) {
            OutSourceCommssion outSourceCommssion = outSourceCommssionRepository.findOne(id);
            if (Objects.nonNull(outSourceCommssion)) {
                outSourceCommssionRepository.delete(id);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }


    /**
     * @return result对象
     * @function 委外佣金报表 204 回款  205 回退  206 修复
     */
    @GetMapping("/outsourceCommissionForm")
    @ApiOperation(value = "委外佣金报表", notes = "委外佣金报表")
    public ResponseEntity<List> outsourceCommissionForm(@RequestParam String companyCode,
                                                        @RequestParam Integer operationType,
                                                        @RequestParam String outsName,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
            } else {
                companyCode = user.getCompanyCode();//限制公司code码
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }

        Object[] objects;
        if (Objects.equals(OutBackSource.operationType.OUTBACKAMT, operationType)) {
            objects = outSourceCommssionRepository.outsourceCommissionReturn(companyCode, operationType, outsName);
        } else if (Objects.equals(OutBackSource.operationType.OUTBACK, operationType)) {
            objects = outSourceCommssionRepository.outsourceCommissionRollback(companyCode, operationType, outsName);
        } else {
            objects = outSourceCommssionRepository.outsourceCommissionRepair(companyCode, operationType, outsName);
        }
        if (objects.length == 1 && Objects.isNull(((Object[]) objects[0])[0])) {
            List kong = new ArrayList();
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(kong);
        }
        List<ObjectReturn> objectReturnList = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            if (Objects.nonNull(((Object[]) objects[i])[0])) {
                Object[] object = (Object[]) objects[i];
                if (0 == objectReturnList.size()) {
                    ObjectReturn objectReturn = new ObjectReturn();
                    ObjectSon objectSon = new ObjectSon();
                    List<ObjectSon> objectSonList = new ArrayList<>();
                    if (Objects.nonNull(object[0].toString())) {
                        objectSon.setOuts_name(object[0].toString());
                    }
                    if (Objects.nonNull(object[1].toString())) {
                        objectSon.setOverdue_time(object[1].toString());
                    }
                    if (Objects.nonNull(object[2].toString())) {
                        objectSon.setMonrybili(new BigDecimal(object[2].toString()));
                    }
                    if (Objects.nonNull(object[3].toString())) {
                        objectSon.setMoney(new BigDecimal(object[3].toString()));
                    }
                    if (Objects.nonNull(object[4].toString())) {
                        objectSon.setHushubili(new BigDecimal(object[4].toString()));
                    }
                    if (Objects.nonNull(object[5].toString())) {
                        objectSon.setNummoney(new BigDecimal(object[5].toString()));
                    }
                    if (Objects.nonNull(object[0].toString())) {
                        objectReturn.setName(object[0].toString());
                    }
                    objectSonList.add(objectSon);
                    objectReturn.setObjectSonList(objectSonList);
                    objectReturnList.add(objectReturn);
                } else {
                    for (ObjectReturn obOld : objectReturnList) {
                        if (Objects.equals(obOld.getName(), object[0].toString())) {
                            ObjectSon objectSon = new ObjectSon();
                            if (Objects.nonNull(object[0].toString())) {
                                objectSon.setOuts_name(object[0].toString());
                            }
                            if (Objects.nonNull(object[1].toString())) {
                                objectSon.setOverdue_time(object[1].toString());
                            }
                            if (Objects.nonNull(object[2].toString())) {
                                objectSon.setMonrybili(new BigDecimal(object[2].toString()));
                            }
                            if (Objects.nonNull(object[3].toString())) {
                                objectSon.setMoney(new BigDecimal(object[3].toString()));
                            }
                            if (Objects.nonNull(object[4].toString())) {
                                objectSon.setHushubili(new BigDecimal(object[4].toString()));
                            }
                            if (Objects.nonNull(object[5].toString())) {
                                objectSon.setNummoney(new BigDecimal(object[5].toString()));
                            }
                            List<ObjectSon> objectSonNew = obOld.getObjectSonList();
                            List<ObjectSon> objectSonList1 = new ArrayList<>();
                            for (ObjectSon objectSon1 : objectSonNew) {
                                objectSonList1.add(objectSon1);
                            }
                            objectSonList1.add(objectSon);
                            obOld.setObjectSonList(objectSonList1);
                        }
                    }
                    boolean a = true;
                    for (ObjectReturn obOld : objectReturnList) {
                        if (Objects.equals(obOld.getName(), object[0].toString())) {
                            a = false;
                        }
                    }
                    if (a) {
                        ObjectReturn objectReturn = new ObjectReturn();
                        ObjectSon objectSon = new ObjectSon();
                        List<ObjectSon> objectSonList = new ArrayList<>();
                        if (Objects.nonNull(object[0].toString())) {
                            objectSon.setOuts_name(object[0].toString());
                        }
                        if (Objects.nonNull(object[1].toString())) {
                            objectSon.setOverdue_time(object[1].toString());
                        }
                        if (Objects.nonNull(object[2].toString())) {
                            objectSon.setMonrybili(new BigDecimal(object[2].toString()));
                        }
                        if (Objects.nonNull(object[3].toString())) {
                            objectSon.setMoney(new BigDecimal(object[3].toString()));
                        }
                        if (Objects.nonNull(object[4].toString())) {
                            objectSon.setHushubili(new BigDecimal(object[4].toString()));
                        }
                        if (Objects.nonNull(object[5].toString())) {
                            objectSon.setNummoney(new BigDecimal(object[5].toString()));
                        }
                        if (Objects.nonNull(object[0].toString())) {
                            objectReturn.setName(object[0].toString());
                        }
                        List<ObjectSon> objectSonList2 = new ArrayList<>();
                        objectSonList2.add(objectSon);
                        objectReturn.setObjectSonList(objectSonList2);
                        objectReturnList.add(objectReturn);
                    }
                }
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(objectReturnList);
    }

    /**
     * @Description 导出回款报表
     */
    @GetMapping("/exportReport")
    @ApiOperation(value = "导出佣金报表", notes = "导出佣金报表")
    public ResponseEntity<String> exportReport(@RequestParam String companyCode,
                                               @RequestParam Integer operationType,
                                               @RequestParam String outsName,
                                               @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
            //判断如果是超级管理员companyCode是为null的
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "请选择公司")).body(null);
                }
            } else {
                companyCode = user.getCompanyCode();//限制公司code码
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }

        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        Object[] objects;
        if (Objects.equals(204, operationType)) {
            objects = outSourceCommssionRepository.outsourceCommissionReturn(companyCode, operationType, outsName);
        } else if (Objects.equals(205, operationType)) {
            objects = outSourceCommssionRepository.outsourceCommissionRollback(companyCode, operationType, outsName);
        } else {
            objects = outSourceCommssionRepository.outsourceCommissionRepair(companyCode, operationType, outsName);
        }
        List<ObjectSonExcel> objectSonList = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            if (Objects.nonNull(((Object[]) objects[i])[0])) {
                Object[] object = (Object[]) objects[i];
                ObjectSonExcel objectSon = new ObjectSonExcel();
                if (Objects.nonNull(object[0].toString())) {
                    objectSon.setOuts_name(object[0].toString());
                }
                if (Objects.nonNull(object[1].toString())) {
                    objectSon.setOverdue_time(object[1].toString());
                }
                if (Objects.nonNull(object[2].toString())) {
                    objectSon.setMonrybili(object[2].toString());
                }
                if (Objects.nonNull(object[3].toString())) {
                    objectSon.setMoney(object[3].toString());
                }
                if (Objects.nonNull(object[4].toString())) {
                    objectSon.setHushubili(object[4].toString());
                }
                if (Objects.nonNull(object[5].toString())) {
                    objectSon.setNummoney(object[5].toString());
                }
                objectSonList.add(objectSon);
            }
        }
        try {
            String[] titleList = {"委外方", "逾期时段", "金额比例", "金额", "户数", "金额(户数)"};
            String[] proNames = {"outs_name", "overdue_time", "monrybili", "money", "hushubili", "nummoney"};
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            ExcelUtil.createExcel(workbook, sheet, objectSonList, titleList, proNames, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "佣金报表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail to upload", "上传服务器失败")).body(null);
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(url.getBody());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "fail", "失败")).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }
}