package cn.fintecher.pangolin.service.reminder.web;


import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.model.MobilePosition;
import cn.fintecher.pangolin.service.reminder.model.MobilePositionParams;
import cn.fintecher.pangolin.service.reminder.model.QMobilePosition;
import cn.fintecher.pangolin.service.reminder.repository.MobilePositionRepository;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author : gaobeibei
 * @Description : 移动定位
 * @Date : 11:28 2017/7/27
 */
@RestController
@RequestMapping(value = "/api/mobilePositionController")
@Api(value = "app移动定位", description = "app移动定位")
public class MobilePositionController{
    private final Logger log = LoggerFactory.getLogger(MobilePositionController.class);

    @Autowired
    MobilePositionRepository mobilePositionRepository;
    @Autowired
    UserClient userClient;
    /**
     * @Description 保存外访人员实时经纬度
     */
    @PostMapping("/saveLatitudeAndLongitude")
    @ApiOperation(value = "保存外访人员实时经纬度", notes = "保存外访人员实时经纬度")
    @ResponseBody
    public ResponseEntity saveLatitudeAndLongitude(@RequestBody MobilePosition mobilePosition, @RequestHeader(value = "X-UserToken") String token) {
        try {
            ResponseEntity<User> userResult = userClient.getUserByToken(token);
            User user = userResult.getBody();
            mobilePosition.setCompanyCode(user.getCompanyCode());
            mobilePosition.setDepCode(user.getDepartment().getCode());
            mobilePosition.setDatetime(ZWDateUtil.getNowDateTime());
            mobilePosition.setRealName(user.getRealName());
            mobilePosition.setUserName(user.getUserName());
            mobilePositionRepository.save(mobilePosition);
            return ResponseEntity.ok().body("保存成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.ok().body("保存失败!");
        }
    }

    /**
     * @Description 多条件搜索
     */
    @GetMapping("/getLatitudeAndLongitude")
    @ApiOperation(value = "多条件搜索", notes = "多条件搜索")
    @ResponseBody
    public ResponseEntity<List<MobilePosition>> getLatitudeAndLongitude(MobilePositionParams mobilePositionParams, @RequestHeader(value = "X-UserToken") String token) {
        List<MobilePosition> mobilePositionList = new ArrayList<>();
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        User user = userResult.getBody();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QMobilePosition.mobilePosition.companyCode.eq(user.getCompanyCode()));
        builder.and(QMobilePosition.mobilePosition.depCode.startsWith(user.getDepartment().getCode()));
        builder.and(QMobilePosition.mobilePosition.userName.ne("administrator"));
        if (null != mobilePositionParams.getName()
                || null != mobilePositionParams.getDepCode()
                || null != mobilePositionParams.getStartDate()
                || null != mobilePositionParams.getEndDate()) {
            if (StringUtils.isNotBlank(mobilePositionParams.getDepCode())) {
                builder.and(QMobilePosition.mobilePosition.depCode.eq(mobilePositionParams.getDepCode()));
            }
            if (StringUtils.isNotBlank(mobilePositionParams.getName())) {
                builder.and(QMobilePosition.mobilePosition.userName.eq(mobilePositionParams.getName()));
            }
            if (Objects.nonNull(mobilePositionParams.getStartDate()) && Objects.nonNull(mobilePositionParams.getEndDate())) {
                builder.and(QMobilePosition.mobilePosition.datetime.between(mobilePositionParams.getStartDate(), mobilePositionParams.getEndDate()));
            } else {
                if (Objects.nonNull(mobilePositionParams.getStartDate())) {
                    builder.and(QMobilePosition.mobilePosition.datetime.after(mobilePositionParams.getStartDate()));
                }
                if (Objects.nonNull(mobilePositionParams.getEndDate())) {
                    builder.and(QMobilePosition.mobilePosition.datetime.before(mobilePositionParams.getEndDate()));
                }
            }
            Iterable<MobilePosition> mobilePositionIterable = mobilePositionRepository.findAll(builder, new Sort(Sort.Direction.DESC, "datetime"));
            mobilePositionIterable.forEach(e->{
                mobilePositionList.add(e);
            });
        } else {
            builder.and(QMobilePosition.mobilePosition.datetime.after(ZWDateUtil.getNightTime(-1)));
            Iterable<MobilePosition> mobilePositionIterable = mobilePositionRepository.findAll(builder, new Sort(Sort.Direction.DESC, "datetime"));
            List<String> nameList = new ArrayList<>();
            mobilePositionIterable.forEach(e->{
                String userName = e.getUserName();
                if(nameList.contains(userName)){
                    nameList.add(userName);
                    mobilePositionList.add(e);
                }
            });
        }
        return new ResponseEntity<>(mobilePositionList, HttpStatus.OK);
    }
}