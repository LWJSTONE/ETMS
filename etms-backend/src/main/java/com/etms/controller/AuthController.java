package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.LoginDTO;
import com.etms.dto.UserDTO;
import com.etms.entity.User;
import com.etms.service.UserService;
import com.etms.vo.LoginVO;
import com.etms.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功", null);
    }
    
    @Operation(summary = "获取当前用户信息")
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
