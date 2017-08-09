package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseRepairRequest;
import cn.fintecher.pangolin.business.model.QueryRequest;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseRepairRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseRepairRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;

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
    private CaseInfoRepository caseInfoRepository;

    /**
     * @Description : 分页,多条件查询状态为未修复或已修复的案件信息
     */
    @GetMapping("/queryCaseRepair")
    @ApiOperation(value = "查询状态为未修复或已修复的案件信息",notes = "查询未修复状态的案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> queryCaseRepair(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token,
                                                            QueryRequest request){
        User user;
        try {
            user = getUserByToken(token);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"The user not login","用户未登陆")).body(null);
        }
        try {
            BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
            if (Objects.nonNull(request.getStatus())) {
                booleanBuilder.and(QCaseRepair.caseRepair.repairStatus.eq(request.getStatus()));
            }
            if (Objects.nonNull(user.getCompanyCode())) {
                booleanBuilder.and(QCaseRepair.caseRepair.companyCode.eq(user.getCompanyCode()));
            }
            Page<CaseRepair> page = caseRepairRepository.findAll(booleanBuilder, pageable);
            return ResponseEntity.ok().body(page);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }

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
            List<CaseRepairRecord> caseRepairRecords = new ArrayList<>();

            for(String fileId : fileIds) {
                // 根据文件id查询附件
                CaseRepairRecord caseRepairRecord = caseRepairRecordRepository.findOne(fileId);
                // 修改状态为已修复
                caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRED.getValue());
                CaseInfo caseInfo = caseRepair.getCaseId();
                // 设置操作时间为现在时间
                caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
                caseRepair.setCaseId(caseInfo);
                caseRepairRecords.add(caseRepairRecord);
                caseInfo.setCaseRepairRecordList(caseRepairRecords);
                caseRepairRepository.save(caseRepair);
            }
            return ResponseEntity.ok().body(request);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("","exception","系统异常")).body(null);
        }
    }
}
