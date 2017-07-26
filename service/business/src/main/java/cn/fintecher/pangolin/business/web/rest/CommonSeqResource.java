package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.SeqCodeRepository;
import cn.fintecher.pangolin.entity.util.LabelValue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:12 2017/7/18
 */
@RestController
@RequestMapping("/api/commonSeqResource")
@Api(value = "commonSeqResource", description = "序列号资源")
public class CommonSeqResource {
    @Autowired
    SeqCodeRepository seqCodeRepository;

    @GetMapping("/getNextSeq")
    @ApiOperation(value = "获取下一个序列号", notes = "获取下一个序列号")
    public ResponseEntity<LabelValue> getNextSeq(@RequestParam @ApiParam("序列名称") String name, @RequestParam @ApiParam("序列长度") Integer length) {
        StringBuilder seq = new StringBuilder();
        String dateStr = LocalDate.now().toString("yyyyMMdd");
        String seqNum = seqCodeRepository.getBatchSeq(name, length);
        if (Objects.equals("prinSeq", name)) {
            seq.append(" ");
            seq.append(seqNum);
        } else {
            seq.append(dateStr).append(seqNum);
        }
        return Optional.ofNullable(new LabelValue(name, seq.toString()))
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
