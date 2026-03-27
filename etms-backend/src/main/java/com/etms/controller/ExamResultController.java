package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.exception.BusinessException;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamResultVO;
import com.etms.vo.ExamResultStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
    @PreAuthorize("hasRole('admin')")
    public Result<PageResult<ExamResultVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
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
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<PageResult<ExamResultVO>> myResults(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
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
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<ExamResultVO> get(@PathVariable Long id) {
        // 权限验证在Service层处理：用户只能查看自己的成绩，管理员可以查看所有成绩
        ExamResultVO vo = examRecordService.getResultDetail(id);
        if (vo == null) {
            throw new BusinessException("成绩记录不存在");
        }
        return Result.success(vo);
    }
    
    @ApiOperation(value = "获取成绩统计")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('admin')")
    public Result<ExamResultStatsVO> getStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        ExamResultStatsVO stats = examRecordService.getResultStats(startTime, endTime);
        return Result.success(stats);
    }
    
    @ApiOperation(value = "导出成绩")
    @GetMapping("/export")
    @PreAuthorize("hasRole('admin')")
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
            String fileName = URLEncoder.encode("成绩报表_" + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            
            // 导出数据
            examRecordService.exportResults(paperId, userId, passed, userName, paperName, startTime, endTime, response.getOutputStream());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }
}
