package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Dept;
import com.etms.service.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/depts")
@RequiredArgsConstructor
public class DeptController {
    
    private final DeptService deptService;
    
    @Operation(summary = "获取部门树形结构")
    @GetMapping("/tree")
    public Result<List<Dept>> tree() {
        List<Dept> tree = deptService.getDeptTree();
        return Result.success(tree);
    }
    
    @Operation(summary = "获取部门列表")
    @GetMapping
    public Result<List<Dept>> list(@RequestParam(required = false) Long parentId) {
        List<Dept> list = deptService.getDeptList(parentId);
        return Result.success(list);
    }
    
    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    public Result<Dept> get(@PathVariable Long id) {
        Dept dept = deptService.getById(id);
        return Result.success(dept);
    }
    
    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Void> add(@RequestBody Dept dept) {
        deptService.addDept(dept);
        return Result.success();
    }
    
    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Dept dept) {
        dept.setId(id);
        deptService.updateDept(dept);
        return Result.success();
    }
    
    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.success();
    }
}
