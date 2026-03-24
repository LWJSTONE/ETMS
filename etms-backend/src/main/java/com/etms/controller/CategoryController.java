package com.etms.controller;

import com.etms.common.Result;
import com.etms.entity.Category;
import com.etms.service.CategoryService;
import com.etms.vo.CategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类控制器
 */
@Api(tags = "分类管理")
@RestController
@RequestMapping("/training/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @ApiOperation(value = "获取分类树形结构")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree(@RequestParam(required = false) Integer categoryType) {
        List<CategoryVO> tree = categoryService.getCategoryTree(categoryType);
        return Result.success(tree);
    }
    
    @ApiOperation(value = "获取分类列表")
    @GetMapping
    public Result<List<CategoryVO>> list(
            @RequestParam(required = false) Integer categoryType,
            @RequestParam(required = false) Long parentId) {
        List<CategoryVO> list = categoryService.getCategoryList(categoryType, parentId);
        return Result.success(list);
    }
    
    @ApiOperation(value = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> get(@PathVariable Long id) {
        CategoryVO vo = categoryService.getCategoryDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增分类")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> add(@Valid @RequestBody Category category) {
        categoryService.addCategory(category);
        return Result.success();
    }
    
    @ApiOperation(value = "更新分类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success();
    }
    
    @ApiOperation(value = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
    
    @ApiOperation(value = "更新分类状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
