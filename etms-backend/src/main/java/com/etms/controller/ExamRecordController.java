package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.ExamRecord;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考试记录控制器
 */
@Api(tags = "考试记录管理")
@RestController
@RequestMapping("/exam/records")
@RequiredArgsConstructor
@Validated
public class ExamRecordController {
    
    private final ExamRecordService examRecordService;
    
    @ApiOperation(value = "分页查询考试记录")
    @GetMapping
    public Result<PageResult<ExamRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        Page<ExamRecord> page = new Page<>(current, size);
        Page<ExamRecordVO> voPage = examRecordService.pageExamRecords(page, paperId, userId, status);
        PageResult<ExamRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取考试记录详情")
    @GetMapping("/{id}")
    public Result<ExamRecordVO> get(@PathVariable Long id) {
        ExamRecordVO vo = examRecordService.getExamRecordDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "开始考试")
    @PostMapping("/start/{paperId}")
    public Result<ExamRecord> startExam(
            @PathVariable Long paperId,
            @RequestParam(required = false) Long planId) {
        ExamRecord record = examRecordService.startExam(paperId, planId);
        return Result.success(record);
    }
    
    @ApiOperation(value = "提交试卷")
    @PostMapping("/submit")
    public Result<Void> submitExam(@RequestBody Map<String, Object> params) {
        Long recordId = Long.valueOf(params.get("recordId").toString());
        String answers = params.get("answers").toString();
        examRecordService.submitExam(recordId, answers);
        return Result.success();
    }
    
    @ApiOperation(value = "获取当前用户的考试记录")
    @GetMapping("/my")
    public Result<PageResult<ExamRecordVO>> pageMy(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        Page<ExamRecord> page = new Page<>(current, size);
        Page<ExamRecordVO> voPage = examRecordService.pageMyExamRecords(page, status);
        PageResult<ExamRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
}
