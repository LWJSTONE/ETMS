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
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<PageResult<LearningProgressVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
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
    public Result<PageResult<LearningProgressVO>> myProgress(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
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
    public Result<Void> updateProgress(@Valid @RequestBody ProgressDTO progressDTO) {
        learningProgressService.updateProgress(
            progressDTO.getPlanId(), 
            progressDTO.getCourseId(), 
            progressDTO.getProgress()
        );
        return Result.success();
    }
    
    @ApiOperation(value = "获取学习进度详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<LearningProgressVO> get(@PathVariable Long id) {
        LearningProgressVO vo = learningProgressService.getProgressDetail(id);
        if (vo == null) {
            throw new BusinessException("学习进度记录不存在");
        }
        return Result.success(vo);
    }
}
