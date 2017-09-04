package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.OutBackSourceRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.OutBackSource;
import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Created by huyanmin
 * Description:
 * Date: 2017-08-31
 */

@RestController
@RequestMapping("/api/outbackSourceController")
@Api(value = "委外回款", description = "委外回款")
public class OutBackSourceController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(OutsourcePoolController.class);

    private static final String ENTITY_NAME = "OutBackSource";

    @Autowired
    private OutBackSourceRepository outbackSourceRepository;
    private OutsourcePoolRepository outsourcePoolRepository;


    /**
     * @Description : 增加委外回款
     */
    @PostMapping("/createOutBackAmt")
    @ApiOperation(value = "委外案件回款", notes = "委外案件回款")
    public ResponseEntity<OutBackSource> createOutBackAmt(@RequestBody OutBackSource outBackSource,
                                                          @RequestHeader(value = "X-UserToken") String token) {

        outBackSource = (OutBackSource) EntityUtil.emptyValueToNull(outBackSource);
        log.debug("REST request to save department : {}", outBackSource);
        if (Objects.isNull(outBackSource.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The company logo cannot be empty", "公司标识不能为空")).body(null);
        }
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            String outId = outBackSource.getOutId();
            OutsourcePool outsourcePool = null;
            if (Objects.nonNull(outId)){
                outsourcePool = outsourcePoolRepository.findOne(outId);
            }
            CaseInfo caseInfo = outsourcePool.getCaseInfo();
            if (Objects.nonNull(caseInfo)){
                outBackSource.setOutcaseId(outsourcePool.getCaseInfo().getId());
            }
            Integer operationType = outBackSource.getOperationType();
            if (OutBackSource.operationType.OUTBACKAMT.getCode().equals(operationType)){
                outsourcePool.setOutBackAmt(outsourcePool.getOutBackAmt().add(outBackSource.getBackAmt()));//累加回款金额
                outsourcePoolRepository.saveAndFlush(outsourcePool);//保存委外案件
            }
            outBackSource.setCompanyCode(user.getCompanyCode());
            outBackSource.setOperator(user.getUserName());
            outBackSource.setOperateTime(ZWDateUtil.getNowDateTime());
            outbackSourceRepository.save(outBackSource);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", " 操作成功")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("操作失败", "", e.getMessage())).body(null);
        }

    }

}
