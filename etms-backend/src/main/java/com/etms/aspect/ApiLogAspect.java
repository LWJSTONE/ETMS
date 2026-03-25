package com.etms.aspect;

import com.etms.entity.OperationLog;
import com.etms.service.LogService;
import com.etms.entity.User;
import com.etms.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * API访问日志切面
 * 记录所有Controller层的方法调用日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLogAspect {
    
    private final LogService logService;
    private final ObjectMapper objectMapper;
    
    /**
     * 切入点：所有Controller方法
     */
    @Pointcut("execution(* com.etms.controller.*.*(..))")
    public void controllerPointcut() {}
    
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 获取当前用户
        Long userId = null;
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            username = authentication.getName();
        }
        
        // 构建日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setUsername(username);
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setIpAddress(getClientIp(request));
        operationLog.setCreateTime(LocalDateTime.now());
        
        Object result = null;
        try {
            result = point.proceed();
            operationLog.setStatus(1); // 成功
            return result;
        } catch (Exception e) {
            operationLog.setStatus(0); // 失败
            operationLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            operationLog.setCostTime((int) costTime);
            
            // 异步保存日志
            try {
                // 获取请求参数
                if (request instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                    byte[] buf = wrapper.getContentAsByteArray();
                    if (buf.length > 0 && buf.length < 5000) {
                        String requestBody = new String(buf, wrapper.getCharacterEncoding());
                        operationLog.setRequestParams(requestBody);
                    }
                }
            } catch (Exception e) {
                log.debug("获取请求参数失败: {}", e.getMessage());
            }
            
            // 异步记录日志
            try {
                logService.saveLogAsync(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
