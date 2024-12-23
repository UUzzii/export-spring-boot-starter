package com.mb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 导出注解
 * @author cxn
 * @date 2024/12/18 11:13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Export {

    /** 导出参数名 */
    String exportParam() default "";

    /** 导出全部参数名 */
    String exportAllParam() default "";

    /** 页码参数名 */
    String pageNumParam() default "";

    /** 每页大小参数名 */
    String pageSizeParam() default "";

    /**
     * 响应数据
     * 如果为空或者default，就直接取方法返回值
     * 如果是统一返回类，那么就写类中数据的字段名，比如data
     * */
    String responseData() default "";

    /** 导出文件文件名称 */
    String fileName() default "导出文件";
}
