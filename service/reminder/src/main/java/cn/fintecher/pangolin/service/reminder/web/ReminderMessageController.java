package cn.fintecher.pangolin.service.reminder.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.model.*;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/api/reminderMessageController")
@Api(value = "提醒消息", description = "提醒消息")
public class ReminderMessageController {

    private final Logger log = LoggerFactory.getLogger(ReminderMessageController.class);

    @Autowired
    private ReminderMessageService reminderMessageService;
    @Autowired
    private ReminderMessageRepository reminderMessageRepository;
    @Autowired
    UserClient userClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/getReminderMessages", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", defaultValue = "0",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", defaultValue = "10",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ", defaultValue = "createTime,desc")
    })
    @ApiOperation(value = "通过登陆用户token查询消息列表", notes = "通过登陆用户token查询消息列表")
    public ResponseEntity<Page<ReminderMessage>> getReminderMessages(@RequestHeader(value = "X-UserToken") String token,@RequestParam("state") String state, Pageable pageable) {
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        User user = userResult.getBody();
        ReminderMessage.ReadStatus readStatus=null;
        if(StringUtils.isNotBlank(state)){
            if(ReminderMessage.ReadStatus.Read.toString().equals(state)){
                readStatus=ReminderMessage.ReadStatus.Read;
            }else if(ReminderMessage.ReadStatus.UnRead.toString().equals(state)){
                readStatus=ReminderMessage.ReadStatus.UnRead;
            }
        }
        return ResponseEntity.ok(reminderMessageService.findByUser(user.getId(), pageable,readStatus));
    }

    @GetMapping("/getReminderMessage/{id}")
    @ApiOperation(value = "获取消息，并会设置为已读", notes = "获取消息，并会设置为已读")
    public ResponseEntity<ReminderMessage> getReminderMessage(@PathVariable String id, @RequestHeader(value = "X-UserToken") String token) throws Exception {

        ReminderMessage message = reminderMessageRepository.findOne(id);
        if (Objects.isNull(message)) {
            return ResponseEntity.notFound().build();
        }
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        if (!userResult.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("无法获取用户", "")).body(null);
        }
        User user = userResult.getBody();

        if (!Objects.equals(user.getId(), message.getUserId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("这不是你本人的消息", "")).body(null);
        }
        message.setState(ReminderMessage.ReadStatus.Read);
        reminderMessageRepository.save(message);
//            sendWebSocketMessage(token, user.getId());
        return ResponseEntity.ok(message);

    }

    @GetMapping("/getWorkbenchReminder")
    @ApiOperation(value = "工作台消息", notes = "会查找3条消息，未读排在前面，其他按时间倒序")
    public ResponseEntity<ReminderWebMessage> getWorkbenchReminder(@RequestHeader(value = "X-UserToken") String token) {
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        User user = userResult.getBody();
        long count = reminderMessageService.countUnRead(user.getId());
        Sort sort = new Sort(Sort.Direction.DESC, "state").and(new Sort(Sort.Direction.DESC, "createTime"));
        Pageable pageable = new PageRequest(0, 3, sort);
        List<ReminderMessage> list = reminderMessageService.findByUser(user.getId(), pageable, ReminderMessage.ReadStatus.UnRead).getContent();
        ReminderWebMessage reminderWebMessage = new ReminderWebMessage();
        reminderWebMessage.setUnReadeCount(count);
        reminderWebMessage.setMessageList(new PageImpl<>(list));
        return ResponseEntity.ok(reminderWebMessage);

    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除消息", notes = "删除消息")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader(value = "X-UserToken") String token) {

        ReminderMessage message = reminderMessageRepository.findOne(id);
        if (Objects.isNull(message)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("无此消息", "")).body(null);

        }
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        User user = userResult.getBody();
        if (!Objects.equals(user.getId(), message.getUserId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("这不是你本人的消息", "")).body(null);
        }
        reminderMessageRepository.delete(message.getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("ReminderMessage", id)).build();
    }

    @PostMapping("/batchDelete")
    @ApiOperation(value = "批量删除消息", notes = "批量删除消息")
    public ResponseEntity<Void> batchDelete(@RequestBody DeleteMessages deleteMessages, @RequestHeader(value = "X-UserToken") String token) {

        if (Objects.isNull(deleteMessages)) {
            ResponseEntity.badRequest().headers(HeaderUtil.createAlert("数据为空", "")).body(null);
        }
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        List<String> ids = deleteMessages.getIds();
        for (String id : ids) {
            ReminderMessage message = reminderMessageRepository.findOne(id);
            reminderMessageRepository.delete(message.getId());
        }
        sendWebSocketMessage(token, userResult.getBody().getId());
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("ReminderMessage", null)).build();

    }

    private void sendWebSocketMessage(String token, String userId) {
        ResponseEntity<ReminderWebMessage> result = getWorkbenchReminder(token);
        if (result.getStatusCode().is2xxSuccessful()) {
            ReminderWebMessage reminderWebMessage = result.getBody();
            List<ReminderMessage> list = reminderWebMessage.getMessageList().getContent();
            ReminderListWebSocketMessage reminderListWebSocketMessage = new ReminderListWebSocketMessage();
            reminderListWebSocketMessage.setData(list);
            reminderListWebSocketMessage.setUnReadeCount(reminderWebMessage.getUnReadeCount());
            userService.sendMessage(userId, reminderListWebSocketMessage);
        }
    }

    @GetMapping("/allMessageIsReaded")
    @ApiOperation(value = "设置所有消息为已读", notes = "设置所有消息为已读")
    public ResponseEntity<Void> makeAllMessageRead(@RequestHeader(value = "X-UserToken") String token) {
        ResponseEntity<User> entity = userClient.getUserByToken(token);
        Query query = new Query();
        query.addCriteria(Criteria.where("state").is(ReminderMessage.ReadStatus.UnRead.toString()));
        query.addCriteria(Criteria.where("userId").is(entity.getBody().getId()));
        Update update = new Update();
        update.set("state", ReminderMessage.ReadStatus.Read);
        mongoTemplate.updateMulti(query, update, ReminderMessage.class);
        sendWebSocketMessage(token, entity.getBody().getId());
        return ResponseEntity.ok().build();

    }

    @GetMapping("/getUnReadCount")
    @ApiOperation(value = "获取未读条数", notes = "获取未读条数")
    public ResponseEntity<Long> getUnReadCount(@RequestHeader(value = "X-UserToken") String token) {
        ResponseEntity<User> entity = userClient.getUserByToken(token);
        long count = reminderMessageService.countUnRead(entity.getBody().getId());
        return ResponseEntity.ok().body(count);

    }

    @PostMapping("/setSelectedMessageReaded")
    @ApiOperation(value = "设置已选定消息为已读" ,notes = "设置已选定消息为已读")
    public ResponseEntity<ReminderMessage> setSelectedMessageReaded(@RequestBody List<String> idList,@RequestHeader(value = "X-UserToken") String token){
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        if (!userResult.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("无法获取用户", "")).body(null);
        }
        User user = userResult.getBody();
        BooleanBuilder builder = new BooleanBuilder();
        QReminderMessage qReminderMessage = QReminderMessage.reminderMessage;
        builder.and(qReminderMessage.id.in(idList)
               .and(qReminderMessage.userId.eq(user.getId())));
        List<ReminderMessage> messageList = new ArrayList<>();
        Iterator<ReminderMessage> it = reminderMessageRepository.findAll(builder).iterator();
        while(it.hasNext()){
            ReminderMessage message = it.next();
            message.setState(ReminderMessage.ReadStatus.Read);
            messageList.add(message);
        }
        if(messageList.size()>0) {
            reminderMessageRepository.save(messageList);
        }
        return ResponseEntity.ok().build();
    }

}
