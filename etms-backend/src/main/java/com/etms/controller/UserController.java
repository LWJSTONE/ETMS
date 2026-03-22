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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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
        userService.updatePassword(id, oldPassword, newPassword);
        return Result.success();
    }
    
    @ApiOperation(value = "重置密码")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @ApiOperation(value = "分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
