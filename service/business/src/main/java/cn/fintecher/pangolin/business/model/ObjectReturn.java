package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-04-19:49
 */
@Data
public class ObjectReturn {
    String name;
    List<ObjectSon> objectSonList;
}
