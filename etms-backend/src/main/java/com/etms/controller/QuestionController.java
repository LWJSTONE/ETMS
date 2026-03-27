package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Question;
import com.etms.exception.BusinessException;
import com.etms.service.QuestionService;
import com.etms.vo.QuestionVO;
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
 * 题库管理控制器
 */
@Api(tags = "题库管理")
@RestController
@RequestMapping("/exam/questions")
@RequiredArgsConstructor
@Validated
public class QuestionController {
    
    private final QuestionService questionService;
    
    @ApiOperation(value = "分页查询题目列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<PageResult<QuestionVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
            @RequestParam(required = false) String questionContent,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Integer status) {
        Page<Question> page = new Page<>(current, size);
        Page<QuestionVO> voPage = questionService.pageQuestions(page, questionContent, questionType, difficulty, courseId, status);
        PageResult<QuestionVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取题目详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<QuestionVO> get(@PathVariable Long id) {
        QuestionVO vo = questionService.getQuestionDetail(id);
        if (vo == null) {
            throw new BusinessException("题目不存在");
        }
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增题目")
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> add(@Valid @RequestBody Question question) {
        questionService.addQuestion(question);
        return Result.success();
    }
    
    @ApiOperation(value = "更新题目")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Question question) {
        question.setId(id);
        questionService.updateQuestion(question);
        return Result.success();
    }
    
    @ApiOperation(value = "删除题目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        // 修复：删除题目前检查是否被试卷引用，统一使用BusinessException
        if (questionService.isUsedInPapers(id)) {
            throw new BusinessException("该题目已被试卷引用，无法删除");
        }
        questionService.deleteQuestion(id);
        return Result.success();
    }
    
    @ApiOperation(value = "随机抽取题目")
    @GetMapping("/random")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<List<QuestionVO>> randomQuestions(
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam @Min(value = 1, message = "抽取题目数量必须大于0") @Max(value = 10000, message = "抽取题目数量不能超过10000") Integer count,
            @RequestParam(required = false) Long courseId) {
        List<QuestionVO> list = questionService.randomQuestions(questionType, difficulty, count, courseId);
        return Result.success(list);
    }
}
