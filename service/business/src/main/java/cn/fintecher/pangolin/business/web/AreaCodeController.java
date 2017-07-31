package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.AreaCodeRepository;
import cn.fintecher.pangolin.business.service.AreaCodeService;
import cn.fintecher.pangolin.entity.AreaCode;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.types.Predicate;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api")
public class AreaCodeController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(AreaCodeController.class);
    private final AreaCodeRepository areaCodeRepository;

    @Autowired
    AreaCodeService areaCodeService;

    public AreaCodeController(AreaCodeRepository areaCodeRepository) {
        this.areaCodeRepository = areaCodeRepository;
    }


    @GetMapping("/areaCode")
    public List<AreaCode> getAllAreaCode() {
        log.debug("REST request to get all AreaCode");
        List<AreaCode> areaCodeList;
        areaCodeList = IteratorUtils.toList(areaCodeService.queryAllAreaCode().iterator());
        return areaCodeList;
    }

    @GetMapping("/queryAreaCode")
    public ResponseEntity<Page<AreaCode>> queryAreaCode(@QuerydslPredicate(root = AreaCode.class) Predicate predicate, @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get all AreaCode");
        Page<AreaCode> page = areaCodeRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryAreaCode");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/areaCode/{id}")
    public ResponseEntity<AreaCode> getAreaCode(@PathVariable Integer id) {
        log.debug("REST request to get areaCode : {}", id);
        AreaCode areaCode = areaCodeService.queryAreaCodeById(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(areaCode));
    }


}
