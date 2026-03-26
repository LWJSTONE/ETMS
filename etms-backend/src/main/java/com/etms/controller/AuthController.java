package com.etms.controller;

import com.etms.annotation.RateLimiter;
import com.etms.common.Result;
import com.etms.dto.LoginDTO;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
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
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final UserService userService;
    private final CaptchaService captchaService;
    
    @RateLimiter(key = "captcha", limit = 10, period = 60, message = "获取验证码过于频繁，请稍后再试")
    @ApiOperation(value = "获取验证码")
    @PostMapping("/captcha")
    public Result<Map<String, String>> getCaptcha(HttpServletRequest request) {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }
    
    @RateLimiter(key = "login", limit = 5, period = 60, message = "登录尝试过于频繁，请稍后再试")
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
            // 修复：返回未登录状态码，前端会根据此跳转到登录页
            throw new BusinessException(401, "未登录");
        }
        UserVO vo = userService.getUserDetail(user.getId());
        if (vo == null) {
            throw new BusinessException(404, "用户信息不存在");
        }
        return Result.success(vo);
    }
}
