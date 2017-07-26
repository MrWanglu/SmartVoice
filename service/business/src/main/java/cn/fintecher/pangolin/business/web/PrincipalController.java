package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.PrincipalRepository;
import cn.fintecher.pangolin.entity.Principal;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 11:46 2017/7/14
 */
@RestController
@RequestMapping("/api/principalController")
@Api(value = "PrincipalController", description = "委托方操作")
public class PrincipalController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(PrincipalController.class);
    @Autowired
    private PrincipalRepository principalRepository;

    @GetMapping("/getPrincipalPageList")
    @ApiOperation(value = "获取委托方分页查询", notes = "获取委托方分页查询")
    public ResponseEntity<Page<Principal>> getPrincipalPageList(@QuerydslPredicate(root = Principal.class) Predicate predicate,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        logger.debug("REST request to get all AreaCode");
        Page<Principal> page = principalRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryAreaCode");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);

    }

    @GetMapping("/getPrincipalList")
    @ApiOperation(value = "获取所有委托方信息", notes = "获取所有委托方信息")
    public ResponseEntity<List<Principal>> getPrincipalPageList() {
        logger.debug("REST request to get all Principal");
        List<Principal> all = principalRepository.findAll();
        return ResponseEntity.ok().body(all);
    }
}
