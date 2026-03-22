package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Role;
import com.etms.service.RoleService;
import com.etms.vo.RoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {
    
    private final RoleService roleService;
    
    @ApiOperation(value = "分页查询角色列表")
    @GetMapping
    public Result<PageResult<RoleVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        Page<Role> page = new Page<>(current, size);
        Page<RoleVO> voPage = roleService.pageRoles(page, roleName, status);
        PageResult<RoleVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取所有角色列表")
    @GetMapping("/all")
    public Result<List<RoleVO>> list() {
        List<RoleVO> list = roleService.listRoles();
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> get(@PathVariable Long id) {
        RoleVO vo = roleService.getRoleDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增角色")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Role role) {
        roleService.addRole(role);
        return Result.success();
    }
    
    @ApiOperation(value = "更新角色")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success();
    }
    
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
    
    @ApiOperation(value = "分配权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
    
    @ApiOperation(value = "获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(id);
        return Result.success(permissionIds);
    }
}
