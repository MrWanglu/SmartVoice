package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.OutsourceInfo;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-10:14
 */
@RestController
@RequestMapping("/api/outsourcePoolController")
@Api(value = "委外管理", description = "委外管理")
public class OutsourcePoolController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(OutsourcePoolController.class);
    //案件批次号最大99999（5位）
    public final static String CASE_SEQ = "caseSeq";
    @Autowired
    private OutsourceRepository outsourceRepository;
    @Autowired
    private BatchSeqService batchSeqService;
    @Autowired
    private CaseInfoRepository caseInfoRepository;
    @Autowired
    private OutsourcePoolRepository outsourcePoolRepository;
    @Autowired
    private OutsourceRecordRepository outsourceRecordRepository;
    private static final String ENTITY_NAME = "OutSource";

    @PostMapping("/outsource")
    @ApiOperation(value = "委外处理", notes = "委外处理")
    public ResponseEntity<Void> batchDistribution(@RequestBody OutsourceInfo outsourceInfo, @RequestHeader(value = "X-UserToken") String token) {
        try {
                List<String> caseIds = outsourceInfo.getCaseIds();//待委外的案件id集合
                List<OutsourceRecord> outsourceRecords = new ArrayList<>();//待保存的案件委外记录集合
                List<CaseInfo> caseInfos = new ArrayList<>();//待保存的委外案件集合
                List<OutsourcePool> outsourcePools = new ArrayList<>();//待保存的流转记录集合
                User user = getUserByToken(token);
                if (Objects.isNull(user)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "获取不到登录人信息")).body(null);
                }
                LabelValue seqResult = batchSeqService.nextSeq(CASE_SEQ, 5);
                String ouorBatch = seqResult.getValue();
                for (String cupoId : caseIds) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(cupoId);
                    if (CaseInfo.CollectionStatus.CASE_OVER.getValue().equals(caseInfo.getCollectionStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "已结案案件不能再委外")).body(null);
                    }
                    Outsource outsource = outsourceRepository.findOne(outsourceInfo.getOutsId());
                    OutsourceRecord outsourceRecord = new OutsourceRecord();
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OUT.getValue());
                    outsourceRecord.setCaseInfo(caseInfo);
                    outsourceRecord.setOutsource(outsource);
                    outsourceRecord.setCreateTime(ZWDateUtil.getNowDateTime());
                    outsourceRecord.setCreator(user.getUserName());
                    outsourceRecord.setFlag(0);//默认正常
                    outsourceRecord.setOuorBatch(ouorBatch);//批次号
                    outsourceRecords.add(outsourceRecord);
                    //将原案件改为已结案
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue());//已委外
                    caseInfo.setEndType(CaseInfo.EndType.OUTSIDE_CLOSED.getValue());//委外结案
                    caseInfo.setOperator(user);
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseInfo.setEndRemark("委外结案");//结案说明
                    caseInfos.add(caseInfo);

                    //保存委外案件
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setOutsource(outsource);
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setOperator(user.getUserName());
                    outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());
                    outsourcePool.setOut_status(OutsourcePool.OutStatus.OUTSIDING.getCode());//委外中
                    outsourcePool.setOutTime(ZWDateUtil.getNowDateTime());
                    outsourcePools.add(outsourcePool);
                }
                //批量保存
                caseInfoRepository.save(caseInfos);
                outsourcePoolRepository.save(outsourcePools);
                outsourceRecordRepository.save(outsourceRecords);
                return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("委外失败", ENTITY_NAME, e.getMessage())).body(null);
        }

    }

}
