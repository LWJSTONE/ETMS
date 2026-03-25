package com.etms.service.impl;

import com.etms.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    // 内存缓存，用于Redis不可用时存储验证码
    private final Map<String, CaptchaEntry> memoryCache = new ConcurrentHashMap<>();
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;
    
    // 使用 SecureRandom 替代 Random，提高安全性
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
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
            memoryCache.put(captchaKey, new CaptchaEntry(captchaText.toLowerCase(), 
                System.currentTimeMillis() + CAPTCHA_EXPIRE_MINUTES * 60 * 1000));
        }
        
        // 生成验证码图片
        String captchaImage = generateCaptchaImage(captchaText);
        
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
     */
    public void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        memoryCache.entrySet().removeIf(entry -> entry.getValue().expireTime < now);
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
     * 生成验证码图片（Base64格式）
     */
    private String generateCaptchaImage(String text) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 填充背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        // 绘制干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(SECURE_RANDOM.nextInt(200), SECURE_RANDOM.nextInt(200), SECURE_RANDOM.nextInt(200)));
            g.drawLine(SECURE_RANDOM.nextInt(IMAGE_WIDTH), SECURE_RANDOM.nextInt(IMAGE_HEIGHT),
                      SECURE_RANDOM.nextInt(IMAGE_WIDTH), SECURE_RANDOM.nextInt(IMAGE_HEIGHT));
        }
        
        // 绘制干扰点
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(SECURE_RANDOM.nextInt(255), SECURE_RANDOM.nextInt(255), SECURE_RANDOM.nextInt(255)));
            g.fillOval(SECURE_RANDOM.nextInt(IMAGE_WIDTH), SECURE_RANDOM.nextInt(IMAGE_HEIGHT), 2, 2);
        }
        
        // 绘制验证码文本
        g.setFont(new Font("Arial", Font.BOLD, 28));
        int x = 10;
        for (int i = 0; i < text.length(); i++) {
            // 随机颜色
            g.setColor(new Color(SECURE_RANDOM.nextInt(100), SECURE_RANDOM.nextInt(100), SECURE_RANDOM.nextInt(100)));
            // 随机角度旋转
            double angle = (SECURE_RANDOM.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, x + 15, 28);
            g.drawString(String.valueOf(text.charAt(i)), x, 30);
            g.rotate(-angle, x + 15, 28);
            x += 26;
        }
        
        g.dispose();
        
        // 转换为Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
}
