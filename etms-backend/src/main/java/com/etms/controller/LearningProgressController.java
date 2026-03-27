package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.ProgressDTO;
import com.etms.entity.UserPlan;
import com.etms.exception.BusinessException;
import com.etms.service.LearningProgressService;
import com.etms.vo.LearningProgressVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 学习进度控制器
 */
@Api(tags = "学习进度管理")
@RestController
@RequestMapping("/training/progress")
@RequiredArgsConstructor
@Validated
public class LearningProgressController {
    
    private final LearningProgressService learningProgressService;
    
    @ApiOperation(value = "分页查询学习进度列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'train_admin', 'dept_manager')")
    public Result<PageResult<LearningProgressVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        Page<LearningProgressVO> page = learningProgressService.pageProgress(current, size, planId, userId, status, userName, planName, sortField, sortOrder);
        PageResult<LearningProgressVO> pageResult = new PageResult<>(
                page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取我的学习进度")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<PageResult<LearningProgressVO>> myProgress(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long planId) {
        Page<LearningProgressVO> page = learningProgressService.getMyProgress(current, size, status, keyword, planId);
        PageResult<LearningProgressVO> pageResult = new PageResult<>(
                page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "更新学习进度")
    @PutMapping
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<Void> updateProgress(@Valid @RequestBody ProgressDTO progressDTO) {
        // 修复：验证用户只能更新自己的学习进度
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        // 修复：添加进度值范围验证
        if (progressDTO.getProgress() == null || progressDTO.getProgress() < 0 || progressDTO.getProgress() > 100) {
            throw new BusinessException("进度值必须在0-100之间");
        }
        
        learningProgressService.updateProgress(
            progressDTO.getPlanId(), 
            progressDTO.getCourseId(), 
            progressDTO.getProgress()
        );
        return Result.success();
    }
    
    @ApiOperation(value = "获取学习进度详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'train_admin', 'dept_manager')")
    public Result<LearningProgressVO> get(@PathVariable Long id) {
        LearningProgressVO vo = learningProgressService.getProgressDetail(id);
        if (vo == null) {
            throw new BusinessException("学习进度记录不存在");
        }
        return Result.success(vo);
    }
}
