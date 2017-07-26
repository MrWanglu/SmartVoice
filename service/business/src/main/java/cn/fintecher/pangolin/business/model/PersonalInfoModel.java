package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.*;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 客户信息详情模型
 * @Date : 11:12 2017/7/26
 */

@Data
public class PersonalInfoModel {
    private Personal personal; //客户基本信息
    private List<PersonalBank> personalBanks; //客户开户信息
    private List<PersonalCar> personalCars; //客户车产信息
    private PersonalJob personalJob; //客户工作单位信息
    private PersonalIncomeExp personalIncomeExp; //客户收支信息
}