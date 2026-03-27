package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Role;
import com.etms.exception.BusinessException;
import com.etms.service.RoleService;
import com.etms.vo.RoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public Result<PageResult<RoleVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
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
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/all")
    public Result<List<RoleVO>> list() {
        List<RoleVO> list = roleService.listRoles();
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取角色详情")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    public Result<RoleVO> get(@PathVariable Long id) {
        RoleVO vo = roleService.getRoleDetail(id);
        if (vo == null) {
            throw new BusinessException("角色不存在");
        }
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增角色")
    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Role role) {
        roleService.addRole(role);
        return Result.success();
    }
    
    @ApiOperation(value = "更新角色")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success();
    }
    
    @ApiOperation(value = "删除角色")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 修复：删除角色前检查是否有用户关联，统一使用BusinessException
        if (roleService.hasUsers(id)) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }
        roleService.deleteRole(id);
        return Result.success();
    }
    
    @ApiOperation(value = "分配权限")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @Valid @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
    
    @ApiOperation(value = "获取角色权限ID列表")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getPermissionIdsByRoleId(id);
        return Result.success(permissionIds);
    }
    
    @ApiOperation(value = "修改角色状态")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody com.etms.dto.StatusDTO statusDTO) {
        roleService.updateStatus(id, statusDTO.getStatus());
        return Result.success();
    }
}
