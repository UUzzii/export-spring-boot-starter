package com.mb.utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GenericTypeUtils {
    
    /**
     * 从Object对象中安全地获取List的元素类型
     * 
     * @param obj 可能是List类型的对象
     * @return Optional<Class<?>> 如果成功获取到类型则返回，否则返回empty
     */
    public static Optional<Class<?>> getListElementType(Object obj) {
        // 检查对象是否为空
        if (obj == null) {
            return Optional.empty();
        }
        
        // 检查对象是否为List类型
        if (!(obj instanceof List)) {
            return Optional.empty();
        }
        
        List<?> list = (List<?>) obj;
        
        // 检查List是否为空
        if (list.isEmpty()) {
            return Optional.empty();
        }
        
        // 获取第一个非空元素
        Object firstElement = list.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
                
        // 如果没有非空元素，返回empty
        if (firstElement == null) {
            return Optional.empty();
        }
        
        // 返回元素的类型
        return Optional.of(firstElement.getClass());
    }
    
    /**
     * 从Object对象中安全地获取List的元素类型，如果无法获取则抛出异常
     * 
     * @param obj 可能是List类型的对象
     * @return Class<?> 列表元素的类型
     * @throws IllegalArgumentException 如果无法获取类型
     */
    public static Class<?> getListElementTypeOrThrow(Object obj) {
        return getListElementType(obj)
                .orElseThrow(() -> new IllegalArgumentException("无法获取List元素类型：对象为null或空集合或非List类型"));
    }
}