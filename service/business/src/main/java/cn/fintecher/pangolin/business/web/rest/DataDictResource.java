package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.DataDictRepository;
import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.entity.QDataDict;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-09-14:15
 */
@RestController
@RequestMapping("/api/dataDictResource")
@Api(value = "数据字典", description = "数据字典")
public class DataDictResource {
    private static final String ENTITY_NAME = "DataDict";
    @Autowired
    DataDictRepository dataDictRepository;

    /**
     * @Description : 通过TypeCode查找数据字典列表
     */
    @GetMapping
    @ApiOperation(value = "通过TypeCode查找数据字典列表", notes = "通过TypeCode查找数据字典列表")
    public ResponseEntity<List<DataDict>> getDataDictByTypeCode(@RequestParam String typeCode) {
        QDataDict qDataDict = QDataDict.dataDict;
        Iterator<DataDict> dataDicts = dataDictRepository.findAll(qDataDict.typeCode.eq(typeCode)).iterator();
        List<DataDict> dataDictList = new ArrayList<>();
        while (dataDicts.hasNext()) {
            dataDictList.add(dataDicts.next());
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取成功", "")).body(dataDictList);
    }
}
