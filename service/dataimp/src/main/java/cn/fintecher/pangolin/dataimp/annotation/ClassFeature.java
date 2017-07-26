package cn.fintecher.pangolin.dataimp.annotation;

import java.lang.annotation.*;

/**
 * @Author: PeiShouWen
 * @Description:  自定义类的注解
 * @Date 14:54 2017/3/4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ClassFeature {
    public String name();
}
