package cn.fintecher.pangolin.common.web;


import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.Feedback;
import cn.fintecher.pangolin.common.model.QFeedback;
import cn.fintecher.pangolin.common.model.RequestFeedback;
import cn.fintecher.pangolin.common.respository.FeedbackRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import io.swagger.annotations.Example;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Objects;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;


/**
 * 反馈信息操作
 * Created by gaobeibei on 2017/8/4.
 */
@RestController
@RequestMapping(value = "/api/feedbackController")
@Api(value = "反馈人信息", description = "反馈人信息")
public class FeedbackController {

    @Autowired
    UserClient userClient;
    @Autowired
    FeedbackRepository feedbackRepository;

    @PostMapping("/saveFeedback")
    @ApiOperation(value = "新增反馈人信息", notes = "新增反馈人信息")
    @ResponseBody
    public ResponseEntity saveFeedback(@RequestBody RequestFeedback request, @RequestHeader(value="X-UserToken") @ApiParam("操作者的token") String token) {

        User user = userClient.getUserByToken(token).getBody();
        if(ZWStringUtils.isEmpty(user)){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Feedback",
                    "", "请先登录")).body(null);
        }
        Feedback feedback = new Feedback();
        BeanUtils.copyProperties(request, feedback);
        feedback.setFeedbackTime(ZWDateUtil.getNowDateTime());
        feedback.setFeedbackName(user.getUserName());
        Feedback ReturnFeedback = feedbackRepository.save(feedback);
        return ResponseEntity.ok().body(ReturnFeedback);
    }

    @DeleteMapping("/deleteFeedback")
    @ApiOperation(value = "删除反馈信息", notes = "删除反馈信息")
    @ResponseBody
    public ResponseEntity deleteFeedback(@ApiParam(value = "反馈id", required = true) @RequestParam String id,@RequestHeader(value="X-UserToken") @ApiParam("操作者的token") String token) {

        User user = userClient.getUserByToken(token).getBody();
        if(ZWStringUtils.isEmpty(user)){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Feedback",
                    "", "请先登录")).body(null);
        }
        feedbackRepository.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @RequestMapping(value = "/queryByName", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "查询反馈信息", notes = "查询反馈信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity queryByName(@ApiParam(value = "反馈姓名") @RequestParam(required = false) String feedbackName, @ApiIgnore Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        if(Objects.nonNull(feedbackName)) {
            builder.and(QFeedback.feedback.feedbackName.eq(feedbackName));
        }
        Page<Feedback> page = feedbackRepository.findAll(builder, pageable);
        return ResponseEntity.ok().body(page);
    }
}
