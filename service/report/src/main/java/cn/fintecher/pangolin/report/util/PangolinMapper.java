package cn.fintecher.pangolin.report.util;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @author : xiaqun
 * @Description : 公共的MAPPER
 * @Date : 9:51 2017/8/1
 */

public interface PangolinMapper<T> extends Mapper<T>,MySqlMapper<T> {
}