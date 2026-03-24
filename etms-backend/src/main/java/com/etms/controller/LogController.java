package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.OperationLog;
import com.etms.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 操作日志控制器
 */
@Api(tags = "日志管理")
@RestController
@RequestMapping("/system/logs")
@RequiredArgsConstructor
@Validated
public class LogController {

    private final LogService logService;

    @ApiOperation(value = "分页查询日志列表")
    @PreAuthorize("hasAuthority('system:log:list')")
    @GetMapping
    public Result<PageResult<OperationLog>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLog> result = logService.pageLogs(page, module, operationType, operator, status, startTime, endTime);

        PageResult<OperationLog> pageResult = new PageResult<>(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()
        );
        return Result.success(pageResult);
    }

    @ApiOperation(value = "获取日志详情")
    @PreAuthorize("hasAuthority('system:log:query')")
    @GetMapping("/{id}")
    public Result<OperationLog> get(@PathVariable Long id) {
        OperationLog log = logService.getLogDetail(id);
        if (log == null) {
            return Result.error("日志不存在");
        }
        return Result.success(log);
    }

    @ApiOperation(value = "清空日志")
    @PreAuthorize("hasAuthority('system:log:clear')")
    @DeleteMapping
    public Result<Void> clear(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        // 修复：添加时间范围参数，防止清空全部日志
        // 必须指定时间范围才能清空，防止误操作
        if (startTime == null || startTime.isEmpty()) {
            return Result.error("清空日志必须指定开始时间");
        }
        if (endTime == null || endTime.isEmpty()) {
            return Result.error("清空日志必须指定结束时间");
        }
        
        logService.clearLogs(startTime, endTime);
        return Result.success();
    }

    @ApiOperation(value = "导出日志")
    @PreAuthorize("hasAuthority('system:log:export')")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletResponse response) {

        logService.exportLogs(module, operationType, operator, status, startTime, endTime, response);
    }
}
