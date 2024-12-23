package com.sky.annotation;// 指定注解所在的包

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)// 指定注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)// 指定注解生命周期为运行时
public @interface AutoFill {
    //数据库操作类型，update insert
    OperationType value();// 指定注解的属性

}
