package cn.fintecher.pangolin.service.reminder.web;


import cn.fintecher.pangolin.entity.ReminderType;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.model.ManyAppmsgRequest;
import cn.fintecher.pangolin.service.reminder.repository.AppMsgRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Created by  gaobeibei.
 * Description:
 * Date: 2017-8-1
 */
@RestController
@RequestMapping(value = "/api/appMsgController")
@Api(value = "app信息推送", description = "app信息推送")
public class AppMsgController{
    private final Logger log = LoggerFactory.getLogger(AppMsgController.class);

    @Autowired
    AppMsgRepository appMsgRepository;
    @Autowired
    AppMsgService appMsgService;
    @Autowired
    UserClient userClient;



    @PostMapping("/saveAppmsg")
    @ApiOperation(value = "新增app信息推送", notes = "新增app信息推送")
    @ResponseBody
    public ResponseEntity saveAppmsg(@RequestBody AppMsg request) {
        AppMsg returnAppMsg = appMsgRepository.save(request);
        appMsgService.sendPush(returnAppMsg);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("推送成功", "")).body(null);
    }

    @PostMapping("/batchSaveAppmsg")
    @ApiOperation(value = "新增app信息批量推送", notes = "新增app信息批量推送")
    @ResponseBody
    public ResponseEntity batchSaveAppmsg(@RequestBody ManyAppmsgRequest request) {
        try {
            for (String id : request.getIds()) {
                AppMsg appmsg = new AppMsg();
                appmsg.setUserId(id);
                appmsg.setTitle(request.getTitle());
                appmsg.setContent(request.getContent());
                appmsg.setType(ReminderType.FLLOWUP);
                appmsg.setAppMsgUnRead(request.getAppmsgNoRead());
                AppMsg ReturnAppmsg = appMsgRepository.save(appmsg);
                appMsgService.sendPush(ReturnAppmsg);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("推送成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("appMsg", "failed", "批量推送消息失败")).body(null);
        }
    }
    @GetMapping("/findById")
    @ApiOperation(value = "信息推送查询", notes = "信息推送查询")
    @ResponseBody
    public ResponseEntity findById(@RequestParam String id) {
        try {
            AppMsg ReturnAppmsg = appMsgRepository.findOne(id);
           // return new Result(Result.ReturnValue.SUCCESS, "", ReturnAppmsg);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("推送成功", "")).body(ReturnAppmsg);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("appMsg", "failed", "查询消息失败")).body(null);
        }
    }
}
