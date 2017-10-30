package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 撤销分案参数
 * @Date : 14:34 2017/10/17
 */

@Data
public class CaseDistributedTemporaryParams {
    private List<String> ids;
}