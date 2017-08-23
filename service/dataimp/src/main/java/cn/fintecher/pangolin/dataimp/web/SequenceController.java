package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.MongoSequence;
import cn.fintecher.pangolin.dataimp.entity.QMongoSequence;
import cn.fintecher.pangolin.dataimp.repository.MongoSequenceRepository;
import cn.fintecher.pangolin.dataimp.service.MongoSequenceService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description: 序列号控制类
 * @Date 16:01 2017/8/11
 */
@RestController
@RequestMapping("/api/sequenceController")
@Api(description = "序列号控制类")
public class SequenceController {

    Logger logger= LoggerFactory.getLogger(SequenceController.class);

    private static final String ENTITY_NAME = "SequenceController";

    @Autowired
    MongoSequenceRepository mongoSequenceRepository;

    @Autowired
    MongoSequenceService mongoSequenceService;
    @GetMapping("/restSequence")
    @ApiOperation(value = "序列重置", notes = "序列重置")
    public ResponseEntity<String> restSequence(@RequestParam String id1,@RequestParam String id2,
                                       @RequestParam String companyCode){
        QMongoSequence qMongoSequence=QMongoSequence.mongoSequence;
        List<String> idList=new ArrayList<>();
        idList.add(id1);
        idList.add(id2);
        Iterable<MongoSequence> mongoSequenceIterable= mongoSequenceRepository.findAll(qMongoSequence.code.in(idList).and(qMongoSequence.companyCode.eq(companyCode)));
        if(!mongoSequenceIterable.iterator().hasNext()){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"restSequence","序列不存在")).body(null);
        }else{
            List<MongoSequence> mongoSequenceList=new ArrayList<>();
            for(Iterator<MongoSequence> iterator=mongoSequenceIterable.iterator();iterator.hasNext();){
                MongoSequence mongoSequence=iterator.next();
                mongoSequence.setCurrentValue(1);
                mongoSequenceList.add(mongoSequence);
            }
         mongoSequenceRepository.save(mongoSequenceList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("序列重置成功",ENTITY_NAME)).body("序列重置成功");
        }
    }

    @GetMapping("/getSequence")
    @ApiOperation(value = "获取序列值", notes = "获取序列值")
    public ResponseEntity<String> getSequence(@RequestParam String code,@RequestParam String companyCode,Integer length){
        try {
            String seq= mongoSequenceService.getNextSeq(code,companyCode,length);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("序列重置成功",ENTITY_NAME)).body(seq);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"restSequence","序列不存在")).body(null);
        }
    }
}
