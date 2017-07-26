package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.RankModel;
import cn.fintecher.pangolin.business.model.UserStatisAppModel;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CasePayApply;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author : gaobeibei
 * @Description : APP首页信息展示
 * @Date : 16:01 2017/7/24
 */
@RestController
@RequestMapping(value = "/api/TotalPageAppController")
@Api(value = "APP首页信息展示", description = "APP首页信息展示")
public class TotalPageAppController extends BaseController {

    final Logger log = LoggerFactory.getLogger(TotalPageAppController.class);
    @Inject
    CasePayApplyRepository casePayApplyRepository;
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @GetMapping(value = "/getTotalPage")
    @ApiOperation(value = "APP首页信息查询", notes = "APP首页信息查询")
    public ResponseEntity<UserStatisAppModel> getTotalPage(@RequestHeader(value = "X-UserToken") String token) throws ParseException {

        UserStatisAppModel userStatisAppModel = new UserStatisAppModel();
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("USER", "user", "用户不存在")).
                    body(null);
        }
        userStatisAppModel.setApplyPayAmt(casePayApplyRepository.queryApplyAmtByUserName(user.getUserName(), CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()));
        userStatisAppModel.setCollectionAmt(caseInfoRepository.getCollectionAmt(user.getId(), CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(ZWDateUtil.getNowDate());
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
        Date endDate = ZWDateUtil.getNowDateTime();
        Date startDate=null;
        Date startDayOfMonth = null;
        if (dayWeek == 1) {
            startDate = ZWDateUtil.getUtilDate(ZWDateUtil.getDate()+" 00:00:00","yyyy-MM-dd HH:mm:ss");
        }
        else
        {
            cal.add(Calendar.DATE, cal.getFirstDayOfWeek()-dayWeek);
            startDate = cal.getTime();
        }
        cal.set(Calendar.DAY_OF_MONTH,1);
        startDayOfMonth = cal.getTime();
        userStatisAppModel.setWeekVisitNum(caseFollowupRecordRepository.getCollectionNum(user.getId(), CaseFollowupRecord.Type.VISIT.getValue(),startDate, endDate));
        userStatisAppModel.setMonthVisitNum(caseFollowupRecordRepository.getCollectionNum(user.getId(), CaseFollowupRecord.Type.VISIT.getValue(),startDayOfMonth, endDate));
        userStatisAppModel.setWeekAssistNum(caseFollowupRecordRepository.getCollectionNum(user.getId(), CaseFollowupRecord.Type.ASSIST.getValue(),startDate, endDate));
        userStatisAppModel.setMonthAssistNum(caseFollowupRecordRepository.getCollectionNum(user.getId(), CaseFollowupRecord.Type.ASSIST.getValue(),startDayOfMonth, endDate));
        userStatisAppModel.setWeekCollectionNum(userStatisAppModel.getWeekVisitNum()+userStatisAppModel.getWeekAssistNum());
        userStatisAppModel.setMonthCollectionNum(userStatisAppModel.getMonthAssistNum()+userStatisAppModel.getMonthVisitNum());
        userStatisAppModel.setPayList(parseRank(casePayApplyRepository.queryPayList(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue(),startDate,endDate)));
        userStatisAppModel.setFollowList(parseRank(caseFollowupRecordRepository.getFlowupCaseList(startDate,endDate)));
        userStatisAppModel.setCollectionList(parseRank(caseFollowupRecordRepository.getCollectionList(startDate,endDate)));
        return new ResponseEntity<>(userStatisAppModel, HttpStatus.OK);
    }

    private List<RankModel> parseRank(List<Object[]> list){
        int rank = 1;
        List<RankModel> rankModelList = new ArrayList<RankModel>();
        for(Object[] object : list){
            RankModel rankModel = new RankModel();
            rankModel.setRank(rank++);
            rankModel.setScore(object[0].toString());
            rankModel.setUserName(object[1].toString());
            rankModel.setUserId(object[2].toString());
            rankModelList.add(rankModel);
        }
        return rankModelList;
    }
}
