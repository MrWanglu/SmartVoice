package cn.fintecher.pangolin.report.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import java.util.Properties;

/**
 * @Author: PeiShouWen
 * @Description: 通用Mybatis Mapper配置
 * @Date 19:02 2017/7/13
 */
@Configuration
public class TyMybatisConfig {
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(){
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setBasePackage("cn.fintecher.pangolin.report.mapper");
        Properties propertiesMapper = new Properties();
        propertiesMapper.setProperty("mappers","tk.mybatis.mapper.common.Mapper");
        propertiesMapper.setProperty("IDENTITY","SELECT REPLACE(UUID(),'-','')");
        propertiesMapper.setProperty("ORDER","BEFORE");
        mapperScannerConfigurer.setProperties(propertiesMapper);
        return mapperScannerConfigurer;
    }
}