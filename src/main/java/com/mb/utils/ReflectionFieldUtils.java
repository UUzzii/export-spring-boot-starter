package com.mb.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionFieldUtils {
    
    /**
     * 获取类及其所有父类的字段（不包括 Object 类的字段）
     *
     * @param clazz 要获取字段的类
     * @return 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        
        // 当前类不为空且不是Object类时，继续向上遍历
        while (clazz != null && clazz != Object.class) {
            // 获取当前类声明的所有字段（包括私有字段）
            Field[] fields = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fields));
            
            // 获取父类，继续遍历
            clazz = clazz.getSuperclass();
        }
        
        return fieldList;
    }

    public static List<Field> getAllFieldsOfName(Class<?> clazz, List<String> fieldNameList) {
        List<Field> allFields = getAllFields(clazz);
        List<Field> resultFields = new ArrayList<>();

        for (Field field : allFields) {
            field.setAccessible(true);
            if (fieldNameList.contains(field.getName())) {
                resultFields.add(field);
            }
        }

        return resultFields;
    }
    
    /**
     * 获取类及其所有父类的特定类型的字段
     *
     * @param clazz 要获取字段的类
     * @param fieldType 字段类型
     * @return 指定类型的字段列表
     */
    public static <T> List<Field> getAllFieldsOfType(Class<?> clazz, Class<T> fieldType) {
        List<Field> allFields = getAllFields(clazz);
        List<Field> resultFields = new ArrayList<>();
        
        for (Field field : allFields) {
            if (fieldType.isAssignableFrom(field.getType())) {
                resultFields.add(field);
            }
        }
        
        return resultFields;
    }
    
    /**
     * 获取类及其所有父类中带有特定注解的字段
     *
     * @param clazz 要获取字段的类
     * @param annotationClass 注解类型
     * @return 带有指定注解的字段列表
     */
    public static List<Field> getAllFieldsWithAnnotation(Class<?> clazz, 
            Class<? extends java.lang.annotation.Annotation> annotationClass) {
        List<Field> allFields = getAllFields(clazz);
        List<Field> resultFields = new ArrayList<>();
        
        for (Field field : allFields) {
            if (field.isAnnotationPresent(annotationClass)) {
                resultFields.add(field);
            }
        }
        
        return resultFields;
    }
}