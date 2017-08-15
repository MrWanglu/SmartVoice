package cn.fintecher.pangolin.dataimp.service;

import cn.fintecher.pangolin.dataimp.entity.MongoSequence;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 18:57 2017/7/19
 */
@Service("mongoSequenceService")
public class MongoSequenceService {
    @Autowired
    MongoTemplate mongo;

    /**
     * 返回指定的序列号
     * @param name
     * @return
     */
    public String getNextSeq(String name,String companyCode) throws Exception{
        MongoSequence mongoSequence=null;
        synchronized (this){
                 mongoSequence = mongo.findAndModify( query(where("_id").is(name).and("companyCode").is(companyCode)),
                    new Update().inc("currentValue", 1),options().upsert(true).returnNew(true),
                    MongoSequence.class);
        }
        if(Objects.isNull(mongoSequence)){
            throw  new Exception("返回序列对象为空");
        }
        String nowDate= ZWDateUtil.getFormatNowDate(Constants.DATE_FORMAT_ONE);
        String seqStr=mongoSequence.getCurrentValue().toString();
        if(StringUtils.length(seqStr)>mongoSequence.getLength()){
            throw  new Exception("超过序列的最大值");
        }
        String seq= ZWStringUtils.formatString(seqStr,mongoSequence.getLength(),3);
        return nowDate.concat(seq);
    }

}
