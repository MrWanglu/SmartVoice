package cn.fintecher.pangolin.entity.message;

import cn.fintecher.pangolin.entity.DataInfoExcelModel;
import cn.fintecher.pangolin.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description: 案件确认消息实体
 * @Date 14:05 2017/7/24
 */
@Data
public class ConfirmDataInfoMessage implements Serializable{
    @ApiModelProperty(notes = "案件对象")
    private List<DataInfoExcelModel> dataInfoExcelModelList;

    @ApiModelProperty(notes = "数据总数")
    private int dataCount;

    @ApiModelProperty(notes = "操作者信息")
    private User user;


}
