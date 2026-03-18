package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.UserDTO;
import com.etms.entity.User;
import com.etms.service.UserService;
import com.etms.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "分页查询用户列表")
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
    
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        UserVO vo = userService.getUserDetail(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }
    
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        userService.updateUser(userDTO);
        return Result.success();
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.updatePassword(id, oldPassword, newPassword);
        return Result.success();
    }
    
    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
    
    @Operation(summary = "修改状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
