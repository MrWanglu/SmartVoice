package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.DataDictRepository;
import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.entity.QDataDict;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-10-9:08
 */
@Service("dataDictService")
public class DataDictService {
    final Logger log = LoggerFactory.getLogger(DataDictService.class);
    private static final String ENTITY_NAME = "DataDict";
    @Autowired
    DataDictRepository dataDictRepository;

    @GetMapping
    @ApiOperation(value = "通过TypeCode查找数据字典列表", notes = "通过TypeCode查找数据字典列表")
    public List<DataDict> getDataDictByTypeCode(@RequestParam String typeCode) {
        QDataDict qDataDict = QDataDict.dataDict;
        Iterator<DataDict> dataDicts = dataDictRepository.findAll(qDataDict.typeCode.eq(typeCode)).iterator();
        List<DataDict> dataDictList = new ArrayList<>();
        while (dataDicts.hasNext()) {
            dataDictList.add(dataDicts.next());
        }
        return dataDictList;
    }

}
