package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.report.mapper.QueryOutsourcePoolMapper;
import cn.fintecher.pangolin.report.model.QueryOutsourcePool;
import cn.fintecher.pangolin.report.model.QueryOutsourcePoolParams;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Date;
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

    @Inject
    QueryOutsourcePoolMapper queryOutsourcePoolMapper;

    @GetMapping("/queryAllOutsourcePool")
    @ApiOperation(value = "委外催收中查询", notes = "委外催收中查询")
    public ResponseEntity<PageInfo> queryAllOutsourcePool(@RequestParam(required = true)@ApiParam(value = "页数") Integer page,
                                                          @RequestParam(required = true)@ApiParam(value = "大小") Integer size,
                                                          @RequestParam(required = false) String companyCode,
                                                          @RequestParam(required = false) String batchNumber,
                                                          @RequestParam(required = false) String outsourceName,
                                                          @RequestParam(required = false) Date outTime,
                                                          @RequestParam(required = false) Date overOutsourceTime){

        try {
            QueryOutsourcePoolParams query = new QueryOutsourcePoolParams();

            if (Objects.nonNull(companyCode)) {
                query.setCompanyCode(companyCode);
            }
            if (Objects.nonNull(batchNumber)) {
                query.setBatchNumber(batchNumber);
            }

            if (Objects.nonNull(outsourceName)) {
                query.setOutsName(outsourceName);
            }

            if (Objects.nonNull(outTime)) {
                query.setOutTime(outTime);
            }
            if (Objects.nonNull(overOutsourceTime)) {
                query.setOverOutsourceTime(overOutsourceTime);
            }
            PageHelper.startPage(page,size);
            List<QueryOutsourcePool> list = queryOutsourcePoolMapper.getAllOutSourcePoolModel(query);
            PageInfo pageInfo = new PageInfo(list);
            return ResponseEntity.ok().body(pageInfo);
        } catch (Exception e) {
           return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("QueryOutsourcePoolController", "queryAllOutsourcePool", e.getMessage())).body(null);
        }

    }
}