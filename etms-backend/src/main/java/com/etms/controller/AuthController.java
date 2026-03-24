package com.etms.controller;

import com.etms.common.Result;
import com.etms.dto.LoginDTO;
import com.etms.entity.User;
import com.etms.service.CaptchaService;
import com.etms.service.UserService;
import com.etms.vo.LoginVO;
import com.etms.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证控制器
 * 
 * 安全说明：
 * 1. 登录接口应添加限流控制，防止暴力破解攻击
 *    建议使用 Spring Security + Redis 实现基于IP的限流，如：
 *    - 同一IP每分钟最多尝试5次登录
 *    - 失败次数过多则临时封禁IP
 *    实现方案参考：
 *    - 使用 @RateLimiter 注解（需自定义切面实现）
 *    - 或使用 Bucket4j/Guava RateLimiter
 *    - 或集成 Spring Cloud Gateway 限流功能
 * 
 * 2. 验证码接口也应添加限流，防止验证码刷新攻击
 *    - 同一IP每分钟最多获取10次验证码
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final UserService userService;
    private final CaptchaService captchaService;
    
    // TODO: 添加限流注解，如 @RateLimiter(key = "'captcha:' + #request.remoteAddr", limit = 10, period = 60)
    @ApiOperation(value = "获取验证码")
    @PostMapping("/captcha")
    public Result<Map<String, String>> getCaptcha(HttpServletRequest request) {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }
    
    // TODO: 添加限流注解，如 @RateLimiter(key = "'login:' + #request.remoteAddr", limit = 5, period = 60)
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        LoginVO loginVO = userService.login(loginDTO, request);
        return Result.success("登录成功", loginVO);
    }
    
    @ApiOperation(value = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return Result.success("登出成功", null);
    }
    
    @ApiOperation(value = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> info() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        UserVO vo = userService.getUserDetail(user.getId());
        return Result.success(vo);
    }
}
