package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.RankModel;
import cn.fintecher.pangolin.business.model.UserStatisAppModel;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.inject.Inject;
import java.text.ParseException;
import java.util.*;

/**
 * @author : gaobeibei
 * @Description : APP首页信息展示
 * @Date : 16:01 2017/7/24
 */
@RestController
@RequestMapping(value = "/api/totalPageAppController")
@Api(value = "APP首页信息展示", description = "APP首页信息展示")
public class TotalPageAppController extends BaseController {

    final Logger log = LoggerFactory.getLogger(TotalPageAppController.class);
    @Inject
    CasePayApplyRepository casePayApplyRepository;
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    UserRepository userRepository;

    @GetMapping(value = "/getTotalPage")
    @ApiOperation(value = "APP首页信息查询", notes = "APP首页信息查询")
    public ResponseEntity<UserStatisAppModel> getTotalPage(@RequestHeader(value = "X-UserToken") String token) throws ParseException {

        UserStatisAppModel userStatisAppModel = new UserStatisAppModel();
        List<RankModel> payList = new ArrayList<RankModel>();
        List<RankModel> followList = new ArrayList<RankModel>();
        List<RankModel> collList = new ArrayList<RankModel>();
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
        payList = parseRank(casePayApplyRepository.queryPayList(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue(),startDate,endDate,User.Type.VISIT.getValue()),user.getId());
        followList = parseRank(caseFollowupRecordRepository.getFlowupCaseList(startDate,endDate,User.Type.VISIT.getValue()),user.getId());
        collList = parseRank(caseFollowupRecordRepository.getCollectionList(startDate,endDate,User.Type.VISIT.getValue()),user.getId());
        if(payList.size() > 0){
            userStatisAppModel.setPersonalPayRank(payList.get(0));
            userStatisAppModel.setPayList(payList.subList(1,payList.size()));
        }
        if(followList.size() > 0){
            userStatisAppModel.setPersonalFollowRank(followList.get(0));
            userStatisAppModel.setFollowList(followList.subList(1,followList.size()));
        }
        if(collList.size() > 0){
            userStatisAppModel.setPersonalCollectionRank(collList.get(0));
            userStatisAppModel.setCollectionList(collList.subList(1,collList.size()));
        }
        return new ResponseEntity<>(userStatisAppModel, HttpStatus.OK);
    }

    private List<RankModel> parseRank(List<Object[]> list, String id){
        int rank = 1;
        List<RankModel> rankModelList = new ArrayList<RankModel>();
        for(Object[] object : list){
            RankModel rankModel = new RankModel();
            rankModel.setRank(rank++);
            if(Objects.nonNull(object[0])){
                rankModel.setScore(object[0].toString());
            }
            if(Objects.nonNull(object[1])) {
                rankModel.setUserName(object[1].toString());
            }
            if(Objects.nonNull(object[2])) {
                rankModel.setUserId(object[2].toString());
            }
            if(Objects.nonNull(object[3])) {
                rankModel.setPhoto(object[3].toString());
            }
            rankModelList.add(rankModel);
            if(id.equals(rankModel.getUserId())){
                rankModelList.add(0,rankModel);
            }
        }
        return rankModelList;
    }
}
