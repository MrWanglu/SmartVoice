package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.model.CaseRepairRequest;
import cn.fintecher.pangolin.business.repository.CaseRepairRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseRepairRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by yuanyanting on 2017/8/8.
 * 案件修复的Controller
 */

@RestController
@RequestMapping("/api/caseRepairController")
@Api(value = "案件修复", description = "案件修复")
public class CaseRepairController extends BaseController{

    @Autowired
    private CaseRepairRepository caseRepairRepository;

    @Autowired
    private CaseRepairRecordRepository caseRepairRecordRepository;

    @Autowired
    private CaseInfoService caseInfoService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * @Description : 修改案件状态到修复完成
     */
    @PostMapping("/toRepair")
    @ApiOperation(value = "修改案件状态",notes = "修改案件状态")
    public ResponseEntity toRepair(@RequestBody CaseRepairRequest request,
                                   @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token){
        User userByToken;
        try {
            userByToken = getUserByToken(token);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "User is not login", "用户未登录")).body(null);
        }
        try {
            // 获取文件的id集合
            List<String> fileIds = request.getFileIds();
            // 案件修复表的id
            CaseRepair caseRepair = caseRepairRepository.findOne(request.getId());
            // 待修复上传的文件集合
            List<CaseRepairRecord> caseRepairRecordList = caseRepair.getCaseRepairRecordList();
            String fileId = "";
            for (String str : fileIds) {
                fileId += str + ",";
            }
            ParameterizedTypeReference<List<UploadFile>> responseType = new ParameterizedTypeReference<List<UploadFile>>(){};
            ResponseEntity<List<UploadFile>> resp = restTemplate.exchange(Constants.FILEID_SERVICE_URL.concat("uploadFile/getAllUploadFileByIds/").concat(fileId.toString()),
                    HttpMethod.GET, null, responseType);
            List<UploadFile> uploadFiles = resp.getBody();
            for (UploadFile uploadFile : uploadFiles) {
                for(String fileId1 : fileIds) {
                    CaseRepairRecord caseRepairRecord = new CaseRepairRecord();
                    caseRepairRecord.setFileId(fileId1);
                    caseRepairRecord.setCaseId(caseRepair.getCaseId().getId());
                    caseRepairRecord.setOperator(userByToken.getUserName());
                    caseRepairRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseRepairRecord.setRepairMemo(request.getRepairMemo());
                    caseRepairRecord.setFileUrl(uploadFile.getUrl());
                    caseRepairRecord.setFileType(uploadFile.getType());
                    caseRepairRecordList.add(caseRepairRecordRepository.saveAndFlush(caseRepairRecord));
                }
            }
            if (!Objects.equals(caseRepair.getRepairStatus(),CaseRepair.CaseRepairStatus.DISTRIBUTE.getValue())) {
                // 修改状态为已修复
                caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRED.getValue());
            }
            // 设置操作时间为现在时间
            caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
            caseRepairRepository.save(caseRepair);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("修改状态成功","toRepair")).body(null);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }

    @RequestMapping(value = "/distributeRepairCase", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "修复分配", notes = "修复分配")
    public ResponseEntity distributeRepairCase(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user = getUserByToken(token);
            caseInfoService.distributeRepairCase(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(null);
        } catch (Exception e) {
            String msg = Objects.isNull(e.getMessage()) ? "系统异常" : e.getMessage();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("RepairCaseDistributeController", "error", msg)).body(null);
        }
    }

    /**
     * @Description 待修复案件查询
     */
    @GetMapping("/getAllRepairingCase")
    @ApiOperation(value = "待修复案件查询", notes = "待修复案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> getAllRepairingCase(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") String token,
                                                                @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CollectionStatus.CASE_OVER.getValue());
        list.add(CaseInfo.CollectionStatus.CASE_OUT.getValue());
        builder.and(QCaseRepair.caseRepair.caseId.collectionStatus.notIn(list));
        List<Integer> list1 = new ArrayList<>();
        list1.add(CaseRepair.CaseRepairStatus.REPAIRING.getValue()); // 待修复
        list1.add(CaseRepair.CaseRepairStatus.REPAIRED.getValue()); // 已修复
        list1.add(CaseRepair.CaseRepairStatus.DISTRIBUTE.getValue()); // 已分配
        builder.and(QCaseRepair.caseRepair.repairStatus.in(list1));
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.nonNull(companyCode)){
                builder.and(QCaseRepair.caseRepair.companyCode.eq(companyCode));
            }
        }else{
            builder.and(QCaseRepair.caseRepair.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseRepair> page = caseRepairRepository.findAll(builder,pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(page);
    }

    /**
     * @Description 已修复案件查询
     */
    @GetMapping("/getAllRepairedCase")
    @ApiOperation(value = "已修复案件查询", notes = "已修复案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> getAllRepairedCase(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                               @ApiIgnore Pageable pageable,
                                                               @RequestHeader(value = "X-UserToken") String token,
                                                               @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.nonNull(companyCode)){
                builder.and(QCaseRepair.caseRepair.companyCode.eq(companyCode));
            }
        }else{
            builder.and(QCaseRepair.caseRepair.companyCode.eq(user.getCompanyCode()));
        }
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CollectionStatus.CASE_OVER.getValue());
        list.add(CaseInfo.CollectionStatus.CASE_OUT.getValue());
        builder.and(QCaseRepair.caseRepair.caseId.collectionStatus.notIn(list));
        builder.and(QCaseRepair.caseRepair.repairStatus.eq(CaseRepair.CaseRepairStatus.REPAIRED.getValue()));
        Page<CaseRepair> page = caseRepairRepository.findAll(builder,pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(page);
    }

    /**
     * @Description 已分配案件查询
     */
    @GetMapping("/getAllDistributeCase")
    @ApiOperation(value = "已分配案件查询", notes = "已分配案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> getAllDistributeCase(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                                 @ApiIgnore Pageable pageable,
                                                                 @RequestHeader(value = "X-UserToken") String token,
                                                                 @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.nonNull(companyCode)){
                builder.and(QCaseRepair.caseRepair.companyCode.eq(companyCode));
            }
        }else{
            builder.and(QCaseRepair.caseRepair.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseRepair.caseRepair.repairStatus.eq(CaseRepair.CaseRepairStatus.DISTRIBUTE.getValue()));
        Page<CaseRepair> page = caseRepairRepository.findAll(builder,pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(page);
    }

    /**
     * @Description ：查看已修复案件信息
     */
    @GetMapping("/viewCaseRepair")
    @ApiOperation(value = "查看已修复案件信息",notes = "查看已修复案件信息")
    public ResponseEntity<List<CaseRepairRecord>> viewCaseRepair(String id) {
        try{
            CaseRepair caseRepair = caseRepairRepository.findOne(id);
            List<CaseRepairRecord> caseRepairRecordList = caseRepair.getCaseRepairRecordList();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查看信息成功","CaseRepairController")).body(caseRepairRecordList);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }

    /**
     * @Description ：修复附件查看
     */
    @GetMapping("/viewCaseRepairRecord")
    @ApiOperation(value = "修复附件查看",notes = "修复附件查看")
    public ResponseEntity<List<CaseRepairRecord>> viewCaseRepairRecord(String id) {
        try{
            Iterable<CaseRepairRecord> caseRepairRecordList = caseRepairRecordRepository.findAll(QCaseRepairRecord.caseRepairRecord.caseId.eq(id));
            List<CaseRepairRecord> list = new ArrayList<>();
            caseRepairRecordList.forEach(single ->list.add(single));
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查看信息成功","CaseRepairController")).body(list);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }

}
