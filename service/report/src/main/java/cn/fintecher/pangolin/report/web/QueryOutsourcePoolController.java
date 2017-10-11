package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.report.mapper.QueryOutsourcePoolMapper;
import cn.fintecher.pangolin.report.model.OutSourcePoolModel;
import cn.fintecher.pangolin.report.model.QueryOutsourcePool;
import cn.fintecher.pangolin.report.model.QueryOutsourcePoolParams;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

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
    public ResponseEntity<OutSourcePoolModel> queryAllOutsourcePool(@RequestParam @ApiParam(value = "页数") Integer page,
                                                                    @RequestParam @ApiParam(value = "大小") Integer size,
                                                                    QueryOutsourcePoolParams queryOutsourcePoolParams) {
        try {
            PageHelper.startPage(page + 1, size);
            List<QueryOutsourcePool> content = queryOutsourcePoolMapper.getAllOutSourcePoolModel(queryOutsourcePoolParams);
            PageInfo pageInfo = new PageInfo(content);
            OutSourcePoolModel outSourcePoolModel = new OutSourcePoolModel();
            outSourcePoolModel.setContent(content);
            outSourcePoolModel.setGetTotalPages(pageInfo.getPages());
            outSourcePoolModel.setGetTotalElements(pageInfo.getTotal());
            return ResponseEntity.ok().body(outSourcePoolModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("QueryOutsourcePoolController", "queryAllOutsourcePool", "委外催收中查询失败")).body(null);
        }

    }
}