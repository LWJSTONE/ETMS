package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.PasswordDTO;
import com.etms.dto.StatusDTO;
import com.etms.dto.UserDTO;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
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
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
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
            throw new BusinessException("未登录");
        }
        
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth) || "admin".equals(auth));
        
        UserVO vo = userService.getUserDetail(id);
        if (vo == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 非管理员只能查看自己的信息
        if (!isAdmin && !currentUsername.equals(vo.getUsername())) {
            throw new BusinessException("无权限查看该用户信息");
        }
        
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@Validated(UserDTO.Add.class) @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }
    
    @ApiOperation(value = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Validated(UserDTO.Update.class) @RequestBody UserDTO userDTO) {
        // 权限校验：管理员可修改所有，普通用户只能修改自己的基本信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth) || "admin".equals(auth));
        
        // 获取目标用户信息
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 非管理员只能修改自己的信息，且不能修改角色、状态等敏感字段
        if (!isAdmin) {
            if (!currentUsername.equals(targetUser.getUsername())) {
                throw new BusinessException("无权限修改其他用户信息");
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
            throw new BusinessException("未登录");
        }
        
        String currentUsername = authentication.getName();
        
        // 获取目标用户信息
        User targetUser = userService.getById(id);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 安全检查：不能删除当前登录用户自己
        if (currentUsername.equals(targetUser.getUsername())) {
            throw new BusinessException("不能删除当前登录用户");
        }
        
        // 安全检查：禁止删除admin账户
        if ("admin".equals(targetUser.getUsername())) {
            throw new BusinessException("admin账户不能删除");
        }
        
        userService.deleteUser(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordDTO passwordDTO) {
        // 权限校验：只能修改自己的密码，或需要管理员权限
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
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
            throw new BusinessException("用户不存在");
        }
        
        // 非管理员只能修改自己的密码
        if (!isAdmin && !currentUsername.equals(targetUser.getUsername())) {
            throw new BusinessException("无权限修改他人密码");
        }
        
        userService.updatePassword(id, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success();
    }
    
    @ApiOperation(value = "重置密码")
    @PutMapping("/{id}/reset-password")
    // 权限校验：只有管理员可以重置密码
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resetPassword(@PathVariable Long id) {
        // 重置密码并返回新密码
        // 管理员可以将新密码通过安全渠道告知用户
        String newPassword = userService.resetPassword(id);
        return Result.success(newPassword);
    }
    
    @ApiOperation(value = "修改状态")
    @PutMapping("/{id}/status")
    // 权限校验：只有管理员可以修改用户状态
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO statusDTO) {
        Integer status = statusDTO.getStatus();
        
        // 修复：防止管理员禁用自己
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            User targetUser = userService.getById(id);
            if (targetUser != null && currentUsername.equals(targetUser.getUsername())) {
                // 管理员不能禁用自己
                if (status == 0) {
                    throw new BusinessException("不能禁用自己的账户");
                }
            }
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
