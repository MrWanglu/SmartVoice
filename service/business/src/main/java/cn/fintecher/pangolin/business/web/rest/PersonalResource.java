package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.entity.Personal;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * Created by zzl029 on 2017/8/22.
 */
@RestController
@RequestMapping("/api/personalResource")
@Api(description = "客户信息池")
public class PersonalResource {
    @Autowired
    private PersonalRepository personalRepository;

    private final Logger log = LoggerFactory.getLogger(PersonalResource.class);
    @GetMapping("/getPerson")
    @ApiOperation(value = "查询客户信息", notes = "查询客户信息")
    public ResponseEntity<Personal> getPerson(@ApiParam(value = "客户id", required = true)@RequestParam String id) throws URISyntaxException {
        log.debug("REST request to get all of CaseInfo");
        Personal personal = personalRepository.findOne(id);
        return new ResponseEntity<>(personal, HttpStatus.OK);
    }
}
