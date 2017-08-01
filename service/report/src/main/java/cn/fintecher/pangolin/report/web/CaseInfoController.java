package cn.fintecher.pangolin.report.web;


import cn.fintecher.pangolin.report.entity.CaseInfo;
import cn.fintecher.pangolin.report.service.CaseInfoService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:59 2017/8/1
 */
@RestController
@RequestMapping("/api/caseInfo")
@Api(description = "委托方数据操作")
public class CaseInfoController {

    Logger logger=LoggerFactory.getLogger(CaseInfoController.class);
    @Autowired
    CaseInfoService caseInfoService;
    @GetMapping("/getCaseInfoAll")
    public ResponseEntity<List<CaseInfo>> getCaseInfoAll() throws URISyntaxException {
        List<CaseInfo> caseInfoList=caseInfoService.getAll(null);

      return  ResponseEntity.created(new URI("/getCaseAll"))
                .headers(HeaderUtil.createEntityCreationAlert("测试", String.valueOf(caseInfoList.size())))
                .body(caseInfoList);
    }
}
