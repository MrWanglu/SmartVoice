package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.entity.MobilePosition;
import cn.fintecher.pangolin.service.reminder.model.MobilePositionParams;
import cn.fintecher.pangolin.service.reminder.repository.MobilePositionRepository;
import cn.fintecher.pangolin.service.reminder.service.MobilePositionService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

/**
 * @author : xiaqun
 * @Description : 移动定位
 * @Date : 11:25 2017/5/31
 */

@Service("mobilePositionService")
public class MobilePositionServiceImpl implements MobilePositionService {
    private final MobilePositionRepository mobilePositionRepository;

    public MobilePositionServiceImpl(MobilePositionRepository mobilePositionRepository) {

        this.mobilePositionRepository = mobilePositionRepository;
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<MobilePosition> getMobilePosition(MobilePositionParams mobilePositionParams, String depCode) throws ParseException {
        Query query = new Query();
        query.addCriteria(Criteria.where("depCode").regex(depCode)); //只查询当前登录用户机构下的记录
        query.addCriteria(Criteria.where("userName").ne("administrator")); //过滤超级管理员
        if (null != mobilePositionParams.getName()
                || null != mobilePositionParams.getDepCode()
                || null != mobilePositionParams.getStartDate()
                || null != mobilePositionParams.getEndDate()) { //有查询条件
            if (StringUtils.isNotBlank(mobilePositionParams.getDepCode())) {
                query.addCriteria(Criteria.where("depCode").regex(mobilePositionParams.getDepCode()));
            }
            if (StringUtils.isNotBlank(mobilePositionParams.getName())) {
                query.addCriteria(Criteria.where("realName").regex(mobilePositionParams.getName()));
            }
            if (Objects.nonNull(mobilePositionParams.getStartDate()) && Objects.nonNull(mobilePositionParams.getEndDate())) {
                query.addCriteria(Criteria.where("datetime").gte(mobilePositionParams.getStartDate()).lte(new Date(mobilePositionParams.getEndDate().getTime())));
            } else {
                if (Objects.nonNull(mobilePositionParams.getStartDate())) {
                    query.addCriteria(Criteria.where("datetime").gte(mobilePositionParams.getStartDate()));
                }
                if (Objects.nonNull(mobilePositionParams.getEndDate())) {
                    query.addCriteria(Criteria.where("datetime").lte(new Date(mobilePositionParams.getEndDate().getTime())));
                }
            }
            return mongoTemplate.find(query.with(new Sort(Sort.Direction.DESC, "datetime")), MobilePosition.class);
        } else { //没有查询条件
            query.addCriteria(Criteria.where("datetime").gte(new Date())); //只查询当天的记录
            List<MobilePosition> mobilePositions = mongoTemplate.find(query.with(new Sort(Sort.Direction.DESC, "datetime")), MobilePosition.class);
            List<MobilePosition> mobilePositionList = new ArrayList<>();
            Iterator<MobilePosition> iterator = mobilePositions.iterator();
            List<String> nameList = new ArrayList<>();
            while (iterator.hasNext()) {
                MobilePosition mobilePosition = iterator.next();
                String userName = mobilePosition.getUserName();
                if (!nameList.contains(userName)) {
                    nameList.add(userName);
                    mobilePositionList.add(mobilePosition);
                }
            }
            return mobilePositionList;
        }
    }


    public void updateDeptCode(Map<String, String> deptUpdateMap) {
        for (Map.Entry entry : deptUpdateMap.entrySet()) {
            Query query = new Query();
            query.addCriteria(Criteria.where("depCode").is(entry.getKey()));
            Update update = new Update();
            update.set("depCode", entry.getValue());
            mongoTemplate.updateMulti(query, update, MobilePosition.class);
        }
    }
}