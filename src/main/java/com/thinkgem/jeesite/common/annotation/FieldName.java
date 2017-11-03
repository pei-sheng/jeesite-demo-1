package com.thinkgem.jeesite.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * bean中文名注解
 */
/* 
 * 方法申明
 * @Target(ElementType.METHOD) 
 * 
 * @Retention 表示在什么级别保存该注解信息。可选的参数值在枚举类型 RetentionPolicy 中，包括： 
          RetentionPolicy.SOURCE 注解将被编译器丢弃 
          RetentionPolicy.CLASS 注解在class文件中可用，但会被VM丢弃 
          RetentionPolicy.RUNTIME VM将在运行期也保留注释，因此可以通过反射机制读取注解的信息。 
 */


@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
public @interface FieldName {
	String value();
}
