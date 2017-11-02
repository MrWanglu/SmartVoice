package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.QueryOutsourcePoolMapper;
import cn.fintecher.pangolin.report.model.OutSourcePoolModel;
import cn.fintecher.pangolin.report.model.QueryOutsourcePool;
import cn.fintecher.pangolin.report.model.QueryOutsourcePoolParams;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * @author : huyanmin
 * @Description : 委外催收
 * @Date : 2017/9/25
 */
@RestController
@RequestMapping("/api/QueryOutsourcePoolController")
@Api(value = "QueryOutsourcePoolController", description = "委外催收中查询")
public class QueryOutsourcePoolController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(QueryOutsourcePoolController.class);


    @Inject
    QueryOutsourcePoolMapper queryOutsourcePoolMapper;

    @GetMapping("/queryAllOutsourcePool")
    @ApiOperation(value = "委外催收中查询", notes = "委外催收中查询")
    public ResponseEntity<OutSourcePoolModel> queryAllOutsourcePool(QueryOutsourcePoolParams queryOutsourcePoolParams,
                                                                    @RequestHeader(value = "X-UserToken") String token) {

        try {
            User tokenUser = getUserByToken(token);
            if (Objects.isNull(tokenUser)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取不到登录人信息", "", "获取不到登录人信息")).body(null);
            }
            PageHelper.startPage(queryOutsourcePoolParams.getPage() + 1, queryOutsourcePoolParams.getSize());
            if(Objects.nonNull(tokenUser.getCompanyCode())) {
                queryOutsourcePoolParams.setCompanyCode(tokenUser.getCompanyCode());
            }
            List<QueryOutsourcePool> content = null;
            if (queryOutsourcePoolParams.getType()==1){
                content = queryOutsourcePoolMapper.getAllOutSourcePoolByBatchNumber(queryOutsourcePoolParams);
            }else {
                content = queryOutsourcePoolMapper.getAllOutSourceByOutsName(queryOutsourcePoolParams);
            }
            PageInfo pageInfo = new PageInfo(content);
            OutSourcePoolModel outSourcePoolModel = new OutSourcePoolModel();
            outSourcePoolModel.setContent(content);
            outSourcePoolModel.setTotalPages(pageInfo.getPages());
            outSourcePoolModel.setTotalElements(pageInfo.getTotal());
            return ResponseEntity.ok().body(outSourcePoolModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("QueryOutsourcePoolController", "queryAllOutsourcePool", "案件查询失败")).body(null);
        }

    }
}