package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.model.BatchDistributeModel;
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
    @PostMapping("/ToRepair")
    @ApiOperation(value = "修改案件状态",notes = "修改案件状态")
    public ResponseEntity ToRepair(@RequestBody CaseRepairRequest request){
        try {
            // 获取文件的id集合
            List<String> fileIds = request.getFileIds();
            // 案件修复表的id
            CaseRepair caseRepair = caseRepairRepository.findOne(request.getId());
            // 待修复上传的文件集合
            List<CaseRepairRecord> caseRepairRecordList = new ArrayList<>();
            for(String fileId : fileIds) {
                CaseRepairRecord caseRepairRecord = new CaseRepairRecord();
                caseRepairRecord.setCaseId(caseRepair.getCaseId().getId());
                caseRepairRecord.setFileId(fileId);
                caseRepairRecord.setOperatorTime(ZWDateUtil.getNowDateTime());
                caseRepairRecord.setRepairMemo(request.getRepairMemo());
                caseRepairRecordList.add(caseRepairRecordRepository.saveAndFlush(caseRepairRecord));
            }
            // 修改状态为已修复
            caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRED.getValue());
            CaseInfo caseInfo = caseRepair.getCaseId();
            // 设置操作时间为现在时间
            caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
            caseRepair.setCaseId(caseInfo);
            caseInfo.setCaseRepairRecordList(caseRepairRecordList);
            caseRepairRepository.save(caseRepair);
            return ResponseEntity.ok().body(request);
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
     * @Description 修复案件查询
     */
    @GetMapping("/getAllRepairedCase")
    @ApiOperation(value = "修复案件查询", notes = "修复案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> getAllTelCase(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseRepair.caseRepair.caseId.companyCode.eq(user.getCompanyCode()));
        Page<CaseRepair> page = caseRepairRepository.findAll(builder,pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(page);
    }

    /**
     * @Description 修复页面获取专员分配信息
     */
    @GetMapping("/getAttachBatchInfo")
    @ApiOperation(value = "修复页面获取专员分配信息", notes = "修复页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getAttachBatchInfo(@RequestHeader(value = "X-UserToken") String token,
                                                                   @ApiParam("催收员ID集合")@RequestParam(required = false) List<String> userIds) {
        try {
            BatchDistributeModel batchDistributeModel = caseInfoService.getAttachBatchDistribution(userIds);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取分配信息成功", "RepairCaseDistributeController")).body(batchDistributeModel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取分配信息失败", "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 修复页面获取机构分配信息
     */
    @GetMapping("/getDeptBatchInfo")
    @ApiOperation(value = "修复页面获取机构分配信息", notes = "修复页面获取机构分配信息")
    public ResponseEntity<Long> getDeptBatchInfo(@RequestHeader(value = "X-UserToken") String token,
                                                 @ApiParam("机构ID")@RequestParam(required = false) String deptId) {
        try {
            Long count = caseInfoService.getDeptBatchDistribution(deptId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取分配信息成功", "RepairCaseDistributeController")).body(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取分配信息失败", "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description ：查看已修复案件信息
     */
    @GetMapping("/ViewCaseRepair")
    @ApiOperation(value = "查看已修复案件信息",notes = "查看已修复案件信息")
    public ResponseEntity<List<UploadFile>> ViewCaseRepair(String id) {
        try{
            List<UploadFile> uploadFiles = null;
            StringBuilder fileIds = new StringBuilder();
            CaseRepair caseRepair = caseRepairRepository.findOne(id);
            CaseInfo caseInfo = caseRepair.getCaseId();
            List<CaseRepairRecord> caseRepairRecordList = caseInfo.getCaseRepairRecordList();
            for(CaseRepairRecord caseRepairRecord : caseRepairRecordList) {
                String fileId = caseRepairRecord.getFileId();
                fileIds.append(fileId).append(",");
            }
            ParameterizedTypeReference<List<UploadFile>> responseType = new ParameterizedTypeReference<List<UploadFile>>(){};
            ResponseEntity<List<UploadFile>> resp = restTemplate.exchange(Constants.FILEID_SERVICE_URL.concat("uploadFile/getAllUploadFileByIds/").concat(fileIds.toString()),
                    HttpMethod.GET, null, responseType);
            uploadFiles = resp.getBody();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查看信息成功","CaseRepairController")).body(uploadFiles);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }

}
