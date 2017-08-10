package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.*;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-09-9:41
 */
@Entity
@Table(name = "data_dict")
@Data
@ApiModel(value = "dataDict", description = "data_dict")
public class DataDict {
    private static final long serialVersionUID = -5643850075856127202L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String typeCode;
    private String code;
    private String name;
    private Integer sort;
}
