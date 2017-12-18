package cn.fintecher.pangolin.dataimp.annotation;

import java.lang.annotation.*;

/**
 * @Author: PeiShouWen
 * @Description: Excel导入注解
 * @Date 15:04 2017/3/3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelAnno {
    /**
     * 在excel文件中某列数据的名称
     *
     * @return 名称
     */
    String cellName() default "";

    /**
     * 实体中的字段类型校验
     * @return
     */

    FieldType fieldType() default FieldType.STRING;
    /**
     * 在excel中列的顺序，从小到大排
     *
     * @return 顺序
     */
    int order() default 0;

    /**
     * 特殊字段校验
     * @return
     */
    FieldCheck fieldCheck() default FieldCheck.NONE;

    /**
     * 数据类型校验枚举
     */
    enum FieldType{
        STRING,
        INTEGER,
        DOUBLE,
        DATE
    }

    /**
     * 特殊字段校验枚举
     */
    enum FieldCheck {
        NONE,
        PERSONAL_NAME,  //客户姓名
        PRODUCT_NAME,   //产品名称
        IDCARD,     //身份证号
        PHONE_NUMBER,   //电话号码
        CASE_AMOUNT,  //案件金额
        PERSONAL_PHONE // 客户手机号码
    }
}
