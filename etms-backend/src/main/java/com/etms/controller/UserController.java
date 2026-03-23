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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN') or hasPermission('system:user:list')")
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
        // 权限校验：管理员或用户本人可查看
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth) || "admin".equals(auth));
        
        UserVO vo = userService.getUserDetail(id);
        if (vo == null) {
            return Result.error("用户不存在");
        }
        
        // 非管理员只能查看自己的信息
        if (!isAdmin && !currentUsername.equals(vo.getUsername())) {
            return Result.error("无权限查看该用户信息");
        }
        
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }
    
    @ApiOperation(value = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        // 权限校验：管理员可修改所有，普通用户只能修改自己的基本信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth) || "admin".equals(auth));
        
        // 获取目标用户信息
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        
        // 非管理员只能修改自己的信息，且不能修改角色、状态等敏感字段
        if (!isAdmin) {
            if (!currentUsername.equals(targetUser.getUsername())) {
                return Result.error("无权限修改其他用户信息");
            }
            // 普通用户不能修改角色和状态
            userDTO.setRoleIds(null);
            userDTO.setStatus(null);
        }
        
        userDTO.setId(id);
        userService.updateUser(userDTO);
        return Result.success();
    }
    
    @ApiOperation(value = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取当前登录用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        
        String currentUsername = authentication.getName();
        
        // 获取目标用户信息
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        
        // 安全检查：不能删除当前登录用户自己
        if (currentUsername.equals(targetUser.getUsername())) {
            return Result.error("不能删除当前登录用户");
        }
        
        userService.deleteUser(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> passwordMap) {
        // 从请求体中获取密码，避免敏感信息暴露在URL中
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");
        
        // 参数校验
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("原密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return Result.error("新密码长度必须在6-20个字符之间");
        }
        
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
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        // 安全校验：密码不再通过API返回，而是通过邮件或短信发送给用户
        // 或者返回一个临时密码token，用户通过该token设置新密码
        userService.resetPassword(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改状态")
    @PutMapping("/{id}/status")
    // 权限校验：只有管理员可以修改用户状态
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    @ApiOperation(value = "导出用户")
    @GetMapping("/export")
    // 权限校验：只有管理员可以导出用户数据
    @PreAuthorize("hasRole('ADMIN')")
    public void export(
            UserDTO userDTO,
            HttpServletResponse response) {
        userService.exportUsers(userDTO, response);
    }
}
