package cn.fintecher.pangolin.service.reminder.web.rest;


import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderTiming;
import cn.fintecher.pangolin.service.reminder.service.ReminderTimingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

@ApiIgnore
@RestController
@RequestMapping("/api/reminderTiming")
public class ReminderTimingResource {

    private final Logger log = LoggerFactory.getLogger(ReminderTimingResource.class);

    @Autowired
    private ReminderTimingService reminderTimingService;


    @PostMapping("/saveReminderTiming")
    public ReminderTiming saveReminderTiming(@RequestBody SendReminderMessage reminderMessage){
        log.debug("REST request to save ReminderTiming : {}", reminderMessage);
        return reminderTimingService.saveReminderTiming(reminderMessage);
    }

    @GetMapping("/sendReminderTiming")
    public List<String> sendReminderTiming(){
        List<String> reminderTimingList = new ArrayList<>();
        for(ReminderTiming reminderTiming: reminderTimingService.getAllReminderTiming()){
            reminderTimingService.sendMessageForReminderTiming(reminderTiming);
            reminderTimingList.add(reminderTiming.getId());
        }
        return reminderTimingList;
    }





}
