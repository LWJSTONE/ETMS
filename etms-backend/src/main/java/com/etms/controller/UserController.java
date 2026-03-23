package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.UserDTO;
import com.etms.entity.User;
import com.etms.service.UserService;
import com.etms.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    
    @ApiOperation(value = "分页查询用户列表")
    @GetMapping
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            UserDTO userDTO) {
        Page<User> page = new Page<>(current, size);
        Page<UserVO> voPage = userService.pageUsers(page, userDTO);
        PageResult<UserVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        UserVO vo = userService.getUserDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增用户")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }
    
    @ApiOperation(value = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        userService.updateUser(userDTO);
        return Result.success();
    }
    
    @ApiOperation(value = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        // 权限校验：只能修改自己的密码，或需要管理员权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        
        // 获取当前登录用户名
        String currentUsername = authentication.getName();
        
        // 检查是否是管理员
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth) || "admin".equals(auth));
        
        // 获取目标用户信息
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        
        // 非管理员只能修改自己的密码
        if (!isAdmin && !currentUsername.equals(targetUser.getUsername())) {
            return Result.error("无权限修改他人密码");
        }
        
        userService.updatePassword(id, oldPassword, newPassword);
        return Result.success();
    }
    
    @ApiOperation(value = "重置密码")
    @PutMapping("/{id}/reset-password")
    // 权限校验：只有管理员可以重置密码
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        // 安全校验：密码不再通过API返回，而是通过邮件或短信发送给用户
        // 或者返回一个临时密码token，用户通过该token设置新密码
        userService.resetPassword(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改状态")
    @PutMapping("/{id}/status")
    // 权限校验：只有管理员可以修改用户状态
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        // 参数验证
        if (status == null) {
            return Result.error("状态不能为空");
        }
        // 验证状态值是否合法（0: 禁用, 1: 启用）
        if (status != 0 && status != 1) {
            return Result.error("状态值不合法");
        }
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @ApiOperation(value = "分配角色")
    @PutMapping("/{id}/roles")
    // 权限校验：只有管理员可以分配角色
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    @ApiOperation(value = "导出用户")
    @GetMapping("/export")
    public void export(
            UserDTO userDTO,
            HttpServletResponse response) {
        userService.exportUsers(userDTO, response);
    }
}
