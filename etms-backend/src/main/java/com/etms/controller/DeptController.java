package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Dept;
import com.etms.exception.BusinessException;
import com.etms.service.DeptService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 部门管理控制器
 */
@Api(tags = "部门管理")
@RestController
@RequestMapping("/system/depts")
@RequiredArgsConstructor
@Validated
public class DeptController {
    
    private final DeptService deptService;
    
    @ApiOperation(value = "获取部门树形结构")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/tree-structure")
    public Result<List<Dept>> tree() {
        List<Dept> tree = deptService.getDeptTree();
        return Result.success(tree);
    }
    
    @ApiOperation(value = "获取部门列表（公开接口，用于报表等模块）")
    @GetMapping("/public/list")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Dept>> publicList() {
        List<Dept> list = deptService.getDeptList(null, null, 1); // 只返回启用状态的部门
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取部门树形结构（公开接口，用于报表等模块）")
    @GetMapping("/public/tree")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Dept>> publicTree() {
        List<Dept> tree = deptService.getDeptTree();
        return Result.success(tree);
    }
    
    @ApiOperation(value = "获取部门列表")
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public Result<List<Dept>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        List<Dept> list = deptService.getDeptList(parentId, deptName, status);
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取部门详情")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    public Result<Dept> get(@PathVariable Long id) {
        Dept dept = deptService.getById(id);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        return Result.success(dept);
    }
    
    @ApiOperation(value = "新增部门")
    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Dept dept) {
        deptService.addDept(dept);
        return Result.success();
    }
    
    @ApiOperation(value = "更新部门")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Dept dept) {
        dept.setId(id);
        deptService.updateDept(dept);
        return Result.success();
    }
    
    @ApiOperation(value = "删除部门")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 检查是否有子部门
        if (deptService.hasChildren(id)) {
            throw new BusinessException("该部门下存在子部门，无法删除");
        }
        // 检查是否有用户
        if (deptService.hasUsers(id)) {
            throw new BusinessException("该部门下存在用户，无法删除");
        }
        deptService.deleteDept(id);
        return Result.success();
    }
}
