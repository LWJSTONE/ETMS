package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamResultVO;
import com.etms.vo.ExamResultStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            @RequestParam(required = false) Integer passed,
            @RequestParam(required = false) String paperName,
            @RequestParam(required = false) String examStartTime,
            @RequestParam(required = false) String examEndTime) {
        Page<ExamResultVO> page = examRecordService.getMyResults(current, size, passed, paperName, examStartTime, examEndTime);
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
    
    @ApiOperation(value = "获取成绩统计")
    @GetMapping("/stats")
    public Result<ExamResultStatsVO> getStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        ExamResultStatsVO stats = examRecordService.getResultStats(startTime, endTime);
        return Result.success(stats);
    }
    
    @ApiOperation(value = "导出成绩")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer passed,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String paperName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("成绩报表_" + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            
            // 导出数据
            examRecordService.exportResults(paperId, userId, passed, userName, paperName, startTime, endTime, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }
}
