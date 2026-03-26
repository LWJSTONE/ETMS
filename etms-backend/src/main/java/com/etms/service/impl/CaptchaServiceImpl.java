package com.etms.service.impl;

import com.etms.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 验证码服务实现类
 * 使用SVG格式生成验证码，不依赖系统图形库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    // 内存缓存，用于Redis不可用时存储验证码
    private final Map<String, CaptchaEntry> memoryCache = new ConcurrentHashMap<>();
    
    // 内存缓存最大容量限制，防止内存溢出
    private static final int MAX_CACHE_SIZE = 10000;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;
    
    // 使用 SecureRandom 替代 Random，提高安全性
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // 颜色数组，用于生成随机颜色
    private static final String[] COLORS = {
        "#1a1a1a", "#2d5a87", "#8b4513", "#006400", "#4b0082", "#8b0000", "#2f4f4f", "#191970"
    };
    
    @Override
    public Map<String, String> generateCaptcha() {
        // 生成验证码文本
        String captchaText = generateRandomText();
        
        // 生成验证码key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        
        // 存储验证码
        if (stringRedisTemplate != null) {
            // 使用Redis存储
            stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaKey, 
                captchaText.toLowerCase(), 
                CAPTCHA_EXPIRE_MINUTES, 
                TimeUnit.MINUTES
            );
        } else {
            // 使用内存缓存
            // 检查缓存大小，如果超过限制则清理过期条目
            if (memoryCache.size() >= MAX_CACHE_SIZE) {
                cleanExpiredCache();
                // 如果清理后仍超过限制，拒绝生成新验证码
                if (memoryCache.size() >= MAX_CACHE_SIZE) {
                    throw new RuntimeException("验证码服务繁忙，请稍后重试");
                }
            }
            memoryCache.put(captchaKey, new CaptchaEntry(captchaText.toLowerCase(), 
                System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000));
        }
        
        // 生成SVG验证码图片
        String captchaImage = generateSvgCaptcha(captchaText);
        
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", captchaImage);
        
        return result;
    }
    
    @Override
    public boolean validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey == null || captcha == null) {
            return false;
        }
        
        String storedCaptcha = null;
        
        if (stringRedisTemplate != null) {
            // 使用Redis验证
            String key = CAPTCHA_PREFIX + captchaKey;
            storedCaptcha = stringRedisTemplate.opsForValue().get(key);
            
            if (storedCaptcha != null) {
                // 验证后删除验证码（一次性使用）
                stringRedisTemplate.delete(key);
            }
        } else {
            // 使用内存缓存验证
            CaptchaEntry entry = memoryCache.get(captchaKey);
            if (entry != null) {
                if (entry.expireTime > System.currentTimeMillis()) {
                    storedCaptcha = entry.captcha;
                }
                // 验证后删除验证码（一次性使用）
                memoryCache.remove(captchaKey);
            }
        }
        
        if (storedCaptcha == null) {
            return false;
        }
        
        return storedCaptcha.equalsIgnoreCase(captcha.trim());
    }
    
    @Override
    public void deleteCaptcha(String captchaKey) {
        if (captchaKey != null) {
            if (stringRedisTemplate != null) {
                stringRedisTemplate.delete(CAPTCHA_PREFIX + captchaKey);
            } else {
                memoryCache.remove(captchaKey);
            }
        }
    }
    
    /**
     * 清理过期的内存缓存
     * 每5分钟执行一次清理任务
     */
    @Scheduled(fixedRate = 300000) // 5分钟执行一次
    public void cleanExpiredCache() {
        if (memoryCache.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        int beforeSize = memoryCache.size();
        memoryCache.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
        int afterSize = memoryCache.size();
        if (beforeSize != afterSize) {
            log.info("清理过期验证码缓存: 清理前={}, 清理后={}, 清理数量={}", beforeSize, afterSize, beforeSize - afterSize);
        }
    }
    
    /**
     * 验证码缓存条目
     */
    private static class CaptchaEntry {
        final String captcha;
        final long expireTime;
        
        CaptchaEntry(String captcha, long expireTime) {
            this.captcha = captcha;
            this.expireTime = expireTime;
        }
    }
    
    /**
     * 生成随机验证码文本
     */
    private String generateRandomText() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成SVG格式验证码图片
     * 不依赖系统图形库，纯文本SVG实现
     */
    private String generateSvgCaptcha(String text) {
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(IMAGE_WIDTH)
           .append("\" height=\"").append(IMAGE_HEIGHT).append("\" viewBox=\"0 0 ")
           .append(IMAGE_WIDTH).append(" ").append(IMAGE_HEIGHT).append("\">");
        
        // 背景
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#f0f0f0\"/>");
        
        // 干扰线
        for (int i = 0; i < 6; i++) {
            String color = COLORS[SECURE_RANDOM.nextInt(COLORS.length)];
            int x1 = SECURE_RANDOM.nextInt(IMAGE_WIDTH);
            int y1 = SECURE_RANDOM.nextInt(IMAGE_HEIGHT);
            int x2 = SECURE_RANDOM.nextInt(IMAGE_WIDTH);
            int y2 = SECURE_RANDOM.nextInt(IMAGE_HEIGHT);
            svg.append("<line x1=\"").append(x1).append("\" y1=\"").append(y1)
               .append("\" x2=\"").append(x2).append("\" y2=\"").append(y2)
               .append("\" stroke=\"").append(color).append("\" stroke-width=\"1\" opacity=\"0.5\"/>");
        }
        
        // 干扰点
        for (int i = 0; i < 30; i++) {
            String color = COLORS[SECURE_RANDOM.nextInt(COLORS.length)];
            int cx = SECURE_RANDOM.nextInt(IMAGE_WIDTH);
            int cy = SECURE_RANDOM.nextInt(IMAGE_HEIGHT);
            svg.append("<circle cx=\"").append(cx).append("\" cy=\"").append(cy)
               .append("\" r=\"1\" fill=\"").append(color).append("\" opacity=\"0.6\"/>");
        }
        
        // 验证码文本
        int x = 15;
        for (int i = 0; i < text.length(); i++) {
            String color = COLORS[SECURE_RANDOM.nextInt(COLORS.length)];
            int rotation = SECURE_RANDOM.nextInt(31) - 15; // -15到15度的随机旋转
            int y = 28 + SECURE_RANDOM.nextInt(6) - 3; // Y轴微调
            
            svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
               .append("\" font-family=\"Arial, sans-serif\" font-size=\"24\" font-weight=\"bold\" ")
               .append("fill=\"").append(color).append("\" ")
               .append("transform=\"rotate(").append(rotation).append(" ").append(x).append(" ").append(y).append(")\">")
               .append(text.charAt(i)).append("</text>");
            
            x += 25;
        }
        
        svg.append("</svg>");
        
        // 返回data URI格式的SVG
        String svgContent = svg.toString();
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svgContent.getBytes());
    }
}
