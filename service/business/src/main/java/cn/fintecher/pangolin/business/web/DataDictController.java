package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.DataDictRepository;
import cn.fintecher.pangolin.business.web.rest.DataDictResource;
import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-09-10:09
 */
@RestController
@RequestMapping("/api/dataDictController")
@Api(value = "数据字典", description = "数据字典")
public class DataDictController extends BaseController {
    private static final String ENTITY_NAME = "login";
    @Autowired
    DataDictRepository dataDictRepository;
    @Autowired
    DataDictResource dataDictResource;

    /**
     * @Description : 获取数据字典返回的hashcode码
     */
    @RequestMapping(value = "/getHashCode", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "获取HashCode", notes = "获取HashCode")
    public ResponseEntity<Map<String, String>> getHashCode() {
        List<DataDict> list = dataDictRepository.findAll();
        String code = String.valueOf(list.hashCode());
        Map<String, String> map = new HashMap<String, String>();
        map.put("dataDictHashCode", code);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取HashCode")).body(map);
    }

    /**
     * @Description : 获取所有的数据字典
     */
    @GetMapping("/getAll")
    @ApiOperation(value = "获取数据字典所有数据", notes = "获取数据字典所有数据")
    public ResponseEntity<List<DataDict>> getAll() {
        List<DataDict> list = dataDictRepository.findAll();
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(list);

    }

    /**
     * @Description : 通过TypeCode查找数据字典列表
     */
    @GetMapping("/getDataDictByTypeCode")
    @ApiOperation(value = "通过TypeCode查找数据字典列表", notes = "通过TypeCode查找数据字典列表")
    public ResponseEntity<List<DataDict>> getDataDictByTypeCode(@RequestParam String typeCode) {
        List<DataDict> dataDictList = dataDictResource.getDataDictByTypeCode(typeCode).getBody();
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }
}

