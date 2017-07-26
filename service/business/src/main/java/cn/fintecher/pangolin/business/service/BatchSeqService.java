package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.BatchSeqRepository;
import cn.fintecher.pangolin.entity.util.LabelValue;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-13:27
 */
@Service("batchSeqService")
public class BatchSeqService {
    final Logger log = LoggerFactory.getLogger(BatchSeqService.class);
    private static final String ENTITY_NAME = "BatchSeq";
    @Autowired
    BatchSeqRepository batchSeqRepository;

    @GetMapping("/nextSeq")
    @ApiOperation(value = "获取下一个序列号", notes = "获取下一个序列号")
    public LabelValue nextSeq(@RequestParam @ApiParam("序列名称") String name,
                              @RequestParam @ApiParam("序列长度") Integer length) {
        StringBuilder seq = new StringBuilder();
        String dateStr = LocalDate.now().toString("yyyyMMdd");
        String seqNum = batchSeqRepository.getBatchSeq(name, length);
        if (Objects.equals("prinSeq", name)) {
            seq.append(" ");
            seq.append(seqNum);
        } else {
            seq.append(dateStr).append(seqNum);
        }
        return new LabelValue(name, seq.toString());
    }
}
