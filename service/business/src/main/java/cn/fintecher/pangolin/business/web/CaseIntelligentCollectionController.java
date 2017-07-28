package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.PersonalContactRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-06-9:44
 */
@RestController
@RequestMapping("/api/caseIntelligentCollectionController")
@Api(value = "智能催收", description = "智能催收")
public class CaseIntelligentCollectionController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(CaseIntelligentCollectionController.class);
    private static final String ENTITY_NAME = "CaseIntelligentCollection";
    private final CaseInfoRepository caseInfoRepository;

    public CaseIntelligentCollectionController(CaseInfoRepository caseInfoRepository) {
        this.caseInfoRepository = caseInfoRepository;
    }

    @Autowired
    private PersonalContactRepository personalContactRepository;

    /**
     * @Description : 分页,多条件查询智能催收案件信息
     */
    @GetMapping("/queryCaseInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> queryCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = getUserByToken(token);
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        if (Objects.nonNull(user.getCompanyCode())) {
            builder.and(QCaseInfo.caseInfo.companyCode.ne(user.getCompanyCode()));
        }
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseIntelligentCollectionController");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

//    /**
//     * @Description 短信群发及语音群呼操作
//     */
//    @PostMapping("/handleBatchSend")
//    @ApiOperation(value = "短信群发及语音群呼操作", notes = "短信群发及语音群呼操作")
//    public ResponseEntity<List<MessageBatchSendRequest>> handleBatchSend(@RequestBody BatchSendRequest request) {
//        //获得案件ID集合
//        List<String> cupoIdlist = request.getCupoIdList();
//        List<CaseInfo> caseInfolList = new ArrayList<CaseInfo>();
//        for (String cupoId : cupoIdlist) {
//            CaseInfo caseInfo = caseInfoRepository.findOne(cupoId);
//            caseInfolList.add(caseInfo);
//        }
//        List<MessageBatchSendRequest> messageBatchSendRequestList = new ArrayList<MessageBatchSendRequest>();
//        Integer selected = request.getSelected(); //是否选择本人
//        List<Integer> selRelations = request.getSelRelationsList(); //客户关系数组
//        for (CaseInfo caseInfo : caseInfolList) {
//            MessageBatchSendRequest messageBatchSendRequest = batchSend(caseInfo, selected, selRelations);
//            messageBatchSendRequest.setCustId(caseInfo.getPersonalInfo().getId());
//            messageBatchSendRequest.setCustName(caseInfo.getPersonalInfo().getName());
//            messageBatchSendRequestList.add(messageBatchSendRequest);
//        }
//        return ResponseEntity.ok().body(messageBatchSendRequestList);
//
//    }

//    /**
//     * @Description 电子邮件群发操作
//     */
//    @PostMapping("/handleEmailSend")
//    @ApiOperation(value = "电子邮件群发操作", notes = "电子邮件群发操作")
//    public ResponseEntity<List<EmailSendRequest>> handleEmailSend(@RequestBody EmailBatchSendRequest emailBatchSendRequest) {
//        //获得案件ID集合
//        List<String> cupoIdlist = emailBatchSendRequest.getEmailBatchSendList();
//        List<CaseInfo> caseInfos = new ArrayList<CaseInfo>();
//        for (String cupoId : cupoIdlist) {
//            CaseInfo caseInfo = caseInfoRepository.findOne(cupoId);
//            caseInfos.add(caseInfo);
//        }
//        List<EmailSendRequest> emailSendRequests = new ArrayList<EmailSendRequest>(); //客户与邮箱集合
//        for (CaseInfo caseInfo : caseInfos) {
//            QPersonalContact qPersonalContact = QPersonalContact.personalContact;
//            //本人的数字码是69
//            Iterable<PersonalContact> personalContacts = personalContactRepository.findAll(qPersonalContact.personalInfo.eq(caseInfo.getPersonalInfo()).and(qPersonalContact.relation.eq(69)));
//            if (personalContacts.iterator().hasNext() && Objects.nonNull(personalContacts.iterator().next().getMail())) {
//                EmailSendRequest emailSendRequest = new EmailSendRequest();
//                emailSendRequest.setCustId(personalContacts.iterator().next().getPersonalInfo().getId());
//                emailSendRequest.setCustName(personalContacts.iterator().next().getName());
//                emailSendRequest.setEmail(personalContacts.iterator().next().getMail());
//                emailSendRequests.add(emailSendRequest);
//            }
//        }
//        return ResponseEntity.ok().body(emailSendRequests);
//    }
//
//    /**
//     * @Description 获得联系人及电话号码，只在短信群发及语音群呼中调用
//     */
//    private MessageBatchSendRequest batchSend(CaseInfo caseInfo, Integer selected, List<Integer> selRelations) {
//        MessageBatchSendRequest messageBatchSendRequest = new MessageBatchSendRequest();
//        List<Integer> relationList = new ArrayList<>(); //客户关系列表
//        List<String> phoneList = new ArrayList<>(); //客户关系的电话列表
//        List<Integer> statusList = new ArrayList<>(); //状态列表
//        List<String> nameList = new ArrayList<>(); //关系人姓名
//        if (1 == selected) { //判断是否选择本人 1：是
//            QPersonalContact qPersonalContact = QPersonalContact.personalContact;
//            //本人的数字码是69
//            Iterable<PersonalContact> personalContacts = personalContactRepository.findAll(qPersonalContact.personalInfo.eq(caseInfo.getPersonalInfo()).and(qPersonalContact.relation.eq(69)));
//            if (personalContacts.iterator().hasNext() && Objects.nonNull(personalContacts.iterator().next().getPhone())) {
//                relationList.add(personalContacts.iterator().next().getRelation());
//                phoneList.add(personalContacts.iterator().next().getPhone());
//                nameList.add(personalContacts.iterator().next().getName());
//                statusList.add(personalContacts.iterator().next().getPhoneStatus());
//            }
//        }
//        List<PersonalContact> personalContactList = new ArrayList<PersonalContact>();
//        QPersonalContact qPersonalContact = QPersonalContact.personalContact;
//        //本人的数字码是69
//        Iterable<PersonalContact> personalContacts = personalContactRepository.findAll(qPersonalContact.personalInfo.eq(caseInfo.getPersonalInfo()).and(qPersonalContact.relation.ne(69)));
//        if (personalContacts.iterator().hasNext()) {
//            personalContactList.add(personalContacts.iterator().next());
//        }
//        for (PersonalContact personalContact : personalContactList) {
//            if (Objects.nonNull(personalContact.getPhone())) {
//                relationList.add(personalContact.getRelation());
//                phoneList.add(personalContact.getPhone());
//                nameList.add(personalContact.getName());
//                statusList.add(personalContact.getPhoneStatus());
//            }
//        }
//        messageBatchSendRequest.setRelation(relationList);
//        messageBatchSendRequest.setPhone(phoneList);
//        messageBatchSendRequest.setStatus(statusList);
//        messageBatchSendRequest.setNameList(nameList);
//        return messageBatchSendRequest;
//    }
}
