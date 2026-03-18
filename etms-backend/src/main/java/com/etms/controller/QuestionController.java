package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Question;
import com.etms.service.QuestionService;
import com.etms.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 题库管理控制器
 */
@Tag(name = "题库管理")
@RestController
@RequestMapping("/exam/questions")
@RequiredArgsConstructor
public class QuestionController {
    
    private final QuestionService questionService;
    
    @Operation(summary = "分页查询题目列表")
    @GetMapping
    public Result<PageResult<QuestionVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String questionContent,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Long courseId) {
        Page<Question> page = new Page<>(current, size);
        Page<QuestionVO> voPage = questionService.pageQuestions(page, questionContent, questionType, difficulty, courseId);
        PageResult<QuestionVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    public Result<QuestionVO> get(@PathVariable Long id) {
        QuestionVO vo = questionService.getQuestionDetail(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增题目")
    @PostMapping
    public Result<Void> add(@RequestBody Question question) {
        questionService.addQuestion(question);
        return Result.success();
    }
    
    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        questionService.updateQuestion(question);
        return Result.success();
    }
    
    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }
    
    @Operation(summary = "随机抽取题目")
    @GetMapping("/random")
    public Result<List<QuestionVO>> randomQuestions(
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam Integer count,
            @RequestParam(required = false) Long courseId) {
        List<QuestionVO> list = questionService.randomQuestions(questionType, difficulty, count, courseId);
        return Result.success(list);
    }
}
