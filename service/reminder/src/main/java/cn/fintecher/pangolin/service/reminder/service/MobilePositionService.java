package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.entity.MobilePosition;
import cn.fintecher.pangolin.service.reminder.model.MobilePositionParams;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author : xiaqun
 * @Description : 移动定位
 * @Date : 11:24 2017/5/31
 */

public interface MobilePositionService {
    /**
     * @Description 多条家查询
     */
    List<MobilePosition> getMobilePosition(MobilePositionParams mobilePositionParams, String token) throws ParseException;

    void updateDeptCode(Map<String, String> deptUpdateMap);
}
