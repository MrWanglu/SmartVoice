package cn.fintecher.pangolin.service.reminder.web.rest;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/2/21.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/reminderMessages")
public class ReminderMessageResource {
    private final Logger log = LoggerFactory.getLogger(ReminderMessageResource.class);
    @Autowired
    private ReminderMessageRepository reminderMessageRepository;
    @Autowired
    private ReminderMessageService reminderMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppMsgService appMsgService;

    @PostMapping
    public ResponseEntity<ReminderMessage> createReminderMessage(@RequestBody SendReminderMessage reminderMessage) throws URISyntaxException {
        log.debug("REST request to save ReminderMessage : {}", reminderMessage);
        ReminderMessage message=new ReminderMessage();
        BeanUtils.copyProperties(reminderMessage,message);
        message.setState(ReminderMessage.ReadStatus.UnRead);
        message.setCreateTime(new Date());
        ReminderMessage result = reminderMessageRepository.save(message);
        ReminderWebSocketMessage reminderWebSocketMessage = new ReminderWebSocketMessage();
        reminderWebSocketMessage.setData(result);
        userService.sendMessage(result.getUserId(), reminderWebSocketMessage);
        if(Objects.nonNull(message.getCcUserIds())){
            for(String userId : message.getCcUserIds()){
                userService.sendMessage(userId , reminderWebSocketMessage);
            }
        }
        if (Objects.nonNull(result)) {
            Long count = reminderMessageService.countUnRead(result.getUserId());
            AppMsg request = new AppMsg();
            BeanUtils.copyProperties(result, request);
            request.setId(null);
            request.setAppMsgUnRead(new Long(count).intValue());
            request.setContent(result.getContent());
            try {
                appMsgService.sendPush(request);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.error("消息推送失败");
            }
        }
        return ResponseEntity.created(new URI("/api/ReminderMessages/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert("ReminderMessage", result.getId()))
                .body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderMessage> getReminderMessage(@PathVariable String id) {
        log.debug("REST request to get ReminderMessage : {}", id);
        ReminderMessage reminderMessage = reminderMessageRepository.findOne(id);
        return Optional.ofNullable(reminderMessage)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<ReminderMessage>> getAllReminderMessage(Pageable pageable)
            throws URISyntaxException {
        log.debug("REST request to get a page of Customers");
        Page<ReminderMessage> page = reminderMessageRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reminderMessages");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminderMessage(@PathVariable String id) {
        log.debug("REST request to delete ReminderMessage : {}", id);
        reminderMessageRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("reminderMessage", id.toString())).build();
    }
}
