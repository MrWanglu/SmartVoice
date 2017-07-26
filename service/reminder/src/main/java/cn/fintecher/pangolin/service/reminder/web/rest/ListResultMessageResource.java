package cn.fintecher.pangolin.service.reminder.web.rest;

import cn.fintecher.pangolin.service.reminder.model.ListResult;
import cn.fintecher.pangolin.service.reminder.model.ListResultMessage;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;

/**
 * Created by ChenChang on 2017/2/21.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/listResultMessageResource")
public class ListResultMessageResource {
    private final Logger log = LoggerFactory.getLogger(ListResultMessageResource.class);
    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<Void> sendResultMessage(@RequestBody ListResult listResult) throws URISyntaxException {
        ListResultMessage listResultMessage = new ListResultMessage();
        listResultMessage.setData(listResult);
        userService.sendMessage(listResultMessage.getData().getUser(), listResultMessage);
        return ResponseEntity.ok().body(null);
    }

}
