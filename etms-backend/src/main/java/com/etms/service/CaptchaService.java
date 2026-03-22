package com.etms.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     * @return 包含验证码key和base64图片的Map
     */
    Map<String, String> generateCaptcha();
    
    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param captcha 验证码
     * @return 是否验证通过
     */
    boolean validateCaptcha(String captchaKey, String captcha);
    
    /**
     * 删除验证码
     * @param captchaKey 验证码key
     */
    void deleteCaptcha(String captchaKey);
}
