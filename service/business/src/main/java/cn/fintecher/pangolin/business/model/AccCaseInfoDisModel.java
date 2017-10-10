package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:案件分配实体
 * @Date 15:17 2017/3/17
 */
@Data
public class AccCaseInfoDisModel  {

    @ApiModelProperty("案件ID List")
    private List<String> caseIdList;

    @ApiModelProperty("部门ID List")
    private List<String> depIdList;

    @ApiModelProperty("用户ID List")
    private List<String> userIdList;

    @ApiModelProperty("分配类型 0-按机构 1-按用户")
    private Integer disType;

    @ApiModelProperty("机构或用户对应分配的案件数量")
    private List<Integer> caseNumList;

    @ApiModelProperty("0 按新计划")
    private Integer isPlan;

    @ApiModelProperty("是否共债优先 0 停用 1 启用")
    private Integer isDebt;

    @ApiModelProperty("是否数量平均 0 停用 1 启用")
    private Integer isNumAvg;

    @ApiModelProperty("计划后的分配的数据")
    private List<AccReceivePoolModel> accReceivePoolModels;


    public enum DisType {
        //按机构
        DEPART_WAY(0),
        //按用户
        USER_WAY(1);
        private Integer value;

        DisType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }
}
