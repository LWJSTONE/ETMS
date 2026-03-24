package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Paper;
import com.etms.service.PaperService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 试卷管理控制器
 */
@Api(tags = "试卷管理")
@RestController
@RequestMapping("/exam/papers")
@RequiredArgsConstructor
@Validated
public class PaperController {
    
    private final PaperService paperService;
    
    @ApiOperation(value = "分页查询试卷列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<PageResult<?>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String paperName,
            @RequestParam(required = false) String paperCode,
            @RequestParam(required = false) Integer status) {
        Page<Paper> page = new Page<>(current, size);
        Page<Paper> voPage = paperService.pagePapers(page, paperName, paperCode, status);
        PageResult<Paper> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取可参加的考试列表")
    @GetMapping("/available")
    public Result<PageResult<?>> available(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        // 普通用户可访问，只返回已发布的试卷
        Page<Paper> page = new Page<>(current, size);
        Page<Paper> voPage = paperService.pagePapers(page, null, null, 1);
        PageResult<Paper> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取试卷详情")
    @GetMapping("/{id}")
    public Result<?> get(@PathVariable Long id, 
                          @RequestParam(required = false, defaultValue = "false") boolean forExam,
                          @RequestParam(required = false) Long planId) {
        // 修复：根据场景区分权限
        // 考试场景(forExam=true)：登录用户可访问，但需验证考试资格
        // 管理场景(forExam=false)：需要管理员权限查看完整信息（包含答案）
        // 当forExam=true且planId不为空时，会验证用户是否有考试资格
        return Result.success(paperService.getPaperDetail(id, forExam, planId));
    }
    
    @ApiOperation(value = "新增试卷")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> add(@Valid @RequestBody Paper paper) {
        paperService.addPaper(paper);
        return Result.success();
    }
    
    @ApiOperation(value = "更新试卷")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Paper paper) {
        paper.setId(id);
        paperService.updatePaper(paper);
        return Result.success();
    }
    
    @ApiOperation(value = "删除试卷")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 修复：删除试卷前检查是否有关联的考试记录
        if (paperService.hasExamRecords(id)) {
            return Result.error("该试卷存在关联的考试记录，无法删除");
        }
        paperService.deletePaper(id);
        return Result.success();
    }
    
    @ApiOperation(value = "发布试卷")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> publish(@PathVariable Long id) {
        paperService.publishPaper(id);
        return Result.success();
    }
    
    @ApiOperation(value = "停用试卷")
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> disable(@PathVariable Long id) {
        paperService.disablePaper(id);
        return Result.success();
    }
}
