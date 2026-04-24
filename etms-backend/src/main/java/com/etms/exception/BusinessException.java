package com.etms.exception;

import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /** 错误码 */
    private final Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 400;  // 修复：业务异常默认code改为400，避免与系统500错误混淆导致前端显示"系统内部错误"
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;  // 修复：业务异常默认code改为400，避免与系统500错误混淆
    }
}
