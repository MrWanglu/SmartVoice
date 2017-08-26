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
import io.swagger.annotations.ApiParam;
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
        userStatisAppModel.setWeekVisitNum(caseFollowupRecordRepository.getCollectionNum(user.getUserName(), CaseFollowupRecord.Type.VISIT.getValue(),startDate, endDate));
        userStatisAppModel.setMonthVisitNum(caseFollowupRecordRepository.getCollectionNum(user.getUserName(), CaseFollowupRecord.Type.VISIT.getValue(),startDayOfMonth, endDate));
        userStatisAppModel.setWeekAssistNum(caseFollowupRecordRepository.getCollectionNum(user.getUserName(), CaseFollowupRecord.Type.ASSIST.getValue(),startDate, endDate));
        userStatisAppModel.setMonthAssistNum(caseFollowupRecordRepository.getCollectionNum(user.getUserName(), CaseFollowupRecord.Type.ASSIST.getValue(),startDayOfMonth, endDate));
        userStatisAppModel.setCommissionAmt(casePayApplyRepository.queryCommission(user.getUserName(),CasePayApply.ApproveStatus.AUDIT_AGREE.getValue(),startDayOfMonth, endDate));
        userStatisAppModel.setWeekCollectionNum(userStatisAppModel.getWeekVisitNum()+userStatisAppModel.getWeekAssistNum());
        userStatisAppModel.setMonthCollectionNum(userStatisAppModel.getMonthAssistNum()+userStatisAppModel.getMonthVisitNum());
        payList = parseRank(casePayApplyRepository.queryPayList(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue(),startDate,endDate,User.Type.VISIT.getValue(),user.getCompanyCode(),user.getDepartment().getCode()),user.getId());
        followList = parseRank(caseFollowupRecordRepository.getFlowupCaseList(startDate,endDate,User.Type.VISIT.getValue(),user.getCompanyCode(),user.getDepartment().getCode()),user.getId());
        collList = parseRank(caseFollowupRecordRepository.getCollectionList(startDate,endDate,User.Type.VISIT.getValue(),user.getCompanyCode(),user.getDepartment().getCode()),user.getId());
        if(payList.size() > 0 && Objects.equals(payList.get(0).getUserId(),user.getId())){
            userStatisAppModel.setPersonalPayRank(payList.get(0));
            if(payList.size()>=11) {
                userStatisAppModel.setPayList(payList.subList(1, 11));
            }else{
                userStatisAppModel.setPayList(payList.subList(1, payList.size()));
            }
        }else{
            if(payList.size()>=10) {
                userStatisAppModel.setPayList(payList.subList(0, 10));
            }else{
                userStatisAppModel.setPayList(payList.subList(0, payList.size()));
            }
        }
        if(followList.size() > 0 && Objects.equals(followList.get(0).getUserId(),user.getId())){
            userStatisAppModel.setPersonalFollowRank(followList.get(0));
            if(followList.size()>=11) {
                userStatisAppModel.setFollowList(followList.subList(1, 11));
            }else{
                userStatisAppModel.setFollowList(followList.subList(1, followList.size()));
            }
        }else{
            if(followList.size()>=10) {
                userStatisAppModel.setFollowList(followList.subList(0, 10));
            }else{
                userStatisAppModel.setFollowList(followList.subList(0, followList.size()));
            }
        }
        if(collList.size() > 0 && Objects.equals(collList.get(0).getUserId(),user.getId())){
            userStatisAppModel.setPersonalCollectionRank(collList.get(0));
            if(collList.size()>=11) {
                userStatisAppModel.setCollectionList(collList.subList(1, 11));
            }else{
                userStatisAppModel.setCollectionList(collList.subList(1, collList.size()));
            }
        }else{
            if(collList.size()>=10) {
                userStatisAppModel.setCollectionList(collList.subList(0, 10));
            }else{
                userStatisAppModel.setCollectionList(collList.subList(0, collList.size()));
            }
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

    @GetMapping(value = "/updatePhoto")
    @ApiOperation(value = "修改用户头像", notes = "修改用户头像")
    public ResponseEntity<User> updatePhoto(@RequestHeader(value = "X-UserToken") String token,
                                            @RequestParam(required = true) @ApiParam(value = "头像ID") String photoUrl){
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        user.setPhoto(photoUrl);
        userRepository.saveAndFlush(user);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert("User", "", "修改头像成功")).body(user);
    }
}
