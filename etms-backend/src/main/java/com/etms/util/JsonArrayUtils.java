package com.etms.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * JSON数组工具类
 * 提供统一的JSON数组解析方法，避免各Service中重复实现
 */
@Slf4j
public class JsonArrayUtils {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private JsonArrayUtils() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 检查ID是否在JSON数组字符串中
     * 
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 "[\"1\", \"2\", \"3\"]"
     * @param targetId 目标ID
     * @return 是否包含该ID
     */
    public static boolean containsId(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = jsonArrayStr.trim();
        if (trimmed.equals("[]") || trimmed.equals("[ ]")) {
            return false;
        }
        
        try {
            List<?> list = OBJECT_MAPPER.readValue(trimmed, List.class);
            for (Object item : list) {
                if (item == null) {
                    continue;
                }
                Long itemId = convertToLong(item);
                if (itemId != null && itemId.equals(targetId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("解析JSON数组失败: {}, 错误: {}", jsonArrayStr, e.getMessage());
            // 降级处理：尝试简单字符串匹配
            return fallbackContainsId(trimmed, targetId);
        }
    }
    
    /**
     * 检查JSON数组是否为空
     * 
     * @param jsonArrayStr JSON数组字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String jsonArrayStr) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return true;
        }
        String str = jsonArrayStr.trim();
        return str.equals("[]") || str.substring(1, str.length() - 1).trim().isEmpty();
    }
    
    /**
     * 检查JSON数组是否不为空
     * 
     * @param jsonArrayStr JSON数组字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String jsonArrayStr) {
        return !isEmpty(jsonArrayStr);
    }
    
    /**
     * 将对象转换为Long类型
     */
    private static Long convertToLong(Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof Number) {
            return ((Number) item).longValue();
        }
        if (item instanceof String) {
            try {
                return Long.parseLong((String) item);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 降级处理：简单字符串匹配
     * 用于JSON解析失败时的备选方案
     */
    private static boolean fallbackContainsId(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || targetId == null) {
            return false;
        }
        // 移除方括号
        String content = jsonArrayStr.replace("[", "").replace("]", "").trim();
        if (content.isEmpty()) {
            return false;
        }
        // 按逗号分割
        String[] parts = content.split(",");
        for (String part : parts) {
            String trimmed = part.trim().replace("\"", "");
            if (targetId.toString().equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
}
