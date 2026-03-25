package com.etms.aspect;

import com.etms.annotation.RateLimiter;
import com.etms.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流切面
 * 基于内存实现的简单限流，适用于单机部署
 * 生产环境建议使用Redis实现分布式限流
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAspect {
    
    /**
     * 存储限流计数器
     * key: 限流key + IP地址
     * value: 包含计数器和过期时间的对象
     */
    private final Map<String, RateCounter> counterMap = new ConcurrentHashMap<>();
    
    /**
     * 清理线程启动标志
     */
    private volatile boolean cleanerStarted = false;
    
    @Around("@annotation(com.etms.annotation.RateLimiter)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 启动清理线程
        startCleanerThread();
        
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        
        if (rateLimiter == null) {
            return point.proceed();
        }
        
        // 获取请求IP
        String ip = getClientIp();
        
        // 构建限流key
        String key = rateLimiter.key() + ":" + ip;
        
        // 检查限流
        if (!tryAcquire(key, rateLimiter.limit(), rateLimiter.period())) {
            log.warn("请求被限流，IP: {}, Key: {}", ip, key);
            throw new BusinessException(429, rateLimiter.message());
        }
        
        return point.proceed();
    }
    
    /**
     * 尝试获取访问许可
     */
    private boolean tryAcquire(String key, int limit, int periodSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - periodSeconds * 1000L;
        
        RateCounter counter = counterMap.compute(key, (k, v) -> {
            if (v == null || v.expireTime < now) {
                return new RateCounter(new AtomicInteger(1), now + periodSeconds * 1000L);
            }
            return v;
        });
        
        // 如果窗口已过期，重置计数器
        if (counter.expireTime < now) {
            counter.count.set(1);
            counter.expireTime = now + periodSeconds * 1000L;
            return true;
        }
        
        // 检查是否超过限制
        int current = counter.count.incrementAndGet();
        return current <= limit;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 启动清理过期计数器的线程
     */
    private synchronized void startCleanerThread() {
        if (!cleanerStarted) {
            cleanerStarted = true;
            Thread cleaner = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(60000); // 每分钟清理一次
                        long now = System.currentTimeMillis();
                        counterMap.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "rate-limiter-cleaner");
            cleaner.setDaemon(true);
            cleaner.start();
        }
    }
    
    /**
     * 限流计数器
     */
    private static class RateCounter {
        AtomicInteger count;
        long expireTime;
        
        RateCounter(AtomicInteger count, long expireTime) {
            this.count = count;
            this.expireTime = expireTime;
        }
    }
}
