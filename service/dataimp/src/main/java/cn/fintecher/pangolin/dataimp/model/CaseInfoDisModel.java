package cn.fintecher.pangolin.dataimp.model;

import cn.fintecher.pangolin.entity.CaseInfoModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * @Author: luqiang
 * @Description:案件分配实体
 * @Date 2017/8/9
 */
@Data
public class CaseInfoDisModel {
    @ApiModelProperty("案件ID List")
    private List<String> caseIdList;

    @ApiModelProperty("部门ID List")
    private List<String> depIdList;

    @ApiModelProperty("用户ID List")
    private List<String> userNameList;

    @ApiModelProperty("分配类型 0-按机构 1-按用户")
    private Integer disType;

    @ApiModelProperty("机构或用户对应分配的案件数量")
    private List<Integer> caseNumList;

    @ApiModelProperty("0 按新计划 1 按原计划")
    private Integer isPlan;

    @ApiModelProperty("计划后的分配的数据")
    private List<CaseInfoModel> caseInfoModels;

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
