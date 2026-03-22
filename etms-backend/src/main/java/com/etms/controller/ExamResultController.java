package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩管理控制器
 */
@Api(tags = "成绩管理")
@RestController
@RequestMapping("/exam/results")
@RequiredArgsConstructor
@Validated
public class ExamResultController {
    
    private final ExamRecordService examRecordService;
    
    @ApiOperation(value = "分页查询成绩列表")
    @GetMapping
    public Result<PageResult<ExamResultVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer passed,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String paperName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<ExamResultVO> page = examRecordService.pageResults(current, size, paperId, userId, passed, userName, paperName, startTime, endTime);
        PageResult<ExamResultVO> pageResult = new PageResult<>(
                page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取我的成绩")
    @GetMapping("/my")
    public Result<PageResult<ExamResultVO>> myResults(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer passed) {
        Page<ExamResultVO> page = examRecordService.getMyResults(current, size, passed);
        PageResult<ExamResultVO> pageResult = new PageResult<>(
                page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取成绩详情")
    @GetMapping("/{id}")
    public Result<ExamResultVO> get(@PathVariable Long id) {
        ExamResultVO vo = examRecordService.getResultDetail(id);
        return Result.success(vo);
    }
}
