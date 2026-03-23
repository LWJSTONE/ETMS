package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Dept;
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
    @GetMapping("/tree-structure")
    public Result<List<Dept>> tree() {
        List<Dept> tree = deptService.getDeptTree();
        return Result.success(tree);
    }
    
    @ApiOperation(value = "获取部门列表")
    @GetMapping
    public Result<List<Dept>> list(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) Integer status) {
        List<Dept> list = deptService.getDeptList(parentId, deptName, status);
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取部门详情")
    @GetMapping("/{id}")
    public Result<Dept> get(@PathVariable Long id) {
        Dept dept = deptService.getById(id);
        return Result.success(dept);
    }
    
    @ApiOperation(value = "新增部门")
    @PreAuthorize("hasAuthority('system:dept:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Dept dept) {
        deptService.addDept(dept);
        return Result.success();
    }
    
    @ApiOperation(value = "更新部门")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Dept dept) {
        dept.setId(id);
        deptService.updateDept(dept);
        return Result.success();
    }
    
    @ApiOperation(value = "删除部门")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }
}
