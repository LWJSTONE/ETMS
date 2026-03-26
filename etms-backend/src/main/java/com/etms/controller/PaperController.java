package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.PaperQuestionDTO;
import com.etms.entity.Paper;
import com.etms.exception.BusinessException;
import com.etms.service.PaperService;
import com.etms.vo.PaperQuestionVO;
import com.etms.vo.PaperVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

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
    public Result<PageResult<Paper>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
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
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<Paper>> available(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
            @RequestParam(required = false) Long planId) {
        // 修复：当planId不为空时，验证用户是否属于该培训计划
        if (planId != null) {
            paperService.validateUserPlanAccess(planId);
        }
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
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制，任何登录用户可访问
    public Result<PaperVO> get(@PathVariable Long id, 
                          @RequestParam(required = false, defaultValue = "false") boolean forExam,
                          @RequestParam(required = false) Long planId) {
        // 修复：根据场景区分权限
        // 考试场景(forExam=true)：登录用户可访问，但需验证考试资格，必须提供planId
        // 管理场景(forExam=false)：需要管理员权限查看完整信息（包含答案）
        if (forExam && planId == null) {
            throw new BusinessException("考试场景下必须提供培训计划ID");
        }
        // 当forExam=true且planId不为空时，会验证用户是否有考试资格
        PaperVO vo = paperService.getPaperDetail(id, forExam, planId);
        if (vo == null) {
            throw new BusinessException("试卷不存在");
        }
        return Result.success(vo);
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
        // 修复：删除试卷前检查是否有关联的考试记录，统一使用BusinessException
        if (paperService.hasExamRecords(id)) {
            throw new BusinessException("该试卷存在关联的考试记录，无法删除");
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
    
    // ==================== 组卷管理接口 ====================
    
    @ApiOperation(value = "获取试卷题目列表")
    @GetMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<List<PaperQuestionVO>> getQuestions(@PathVariable Long id) {
        List<PaperQuestionVO> questions = paperService.getPaperQuestions(id);
        return Result.success(questions);
    }
    
    @ApiOperation(value = "批量添加题目到试卷")
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> addQuestions(@PathVariable Long id, @Valid @RequestBody List<@Valid PaperQuestionDTO> questions) {
        paperService.batchAddQuestions(id, questions);
        return Result.success();
    }
    
    @ApiOperation(value = "从试卷移除单个题目")
    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> removeQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        paperService.removeQuestionFromPaper(id, questionId);
        return Result.success();
    }
    
    @ApiOperation(value = "清空试卷所有题目")
    @DeleteMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> clearQuestions(@PathVariable Long id) {
        paperService.clearPaperQuestions(id);
        return Result.success();
    }
}
