package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.OperationLog;
import com.etms.exception.BusinessException;
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
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public Result<PageResult<OperationLog>> page(
            @RequestParam(defaultValue = "1") @javax.validation.constraints.Min(value = 1, message = "页码最小为1") Long current,
            @RequestParam(defaultValue = "10") @javax.validation.constraints.Min(value = 1, message = "每页条数最小为1") @javax.validation.constraints.Max(value = 10000, message = "每页条数最大为10000") Long size,
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
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    public Result<OperationLog> get(@PathVariable Long id) {
        OperationLog log = logService.getLogDetail(id);
        if (log == null) {
            throw new BusinessException("日志不存在");
        }
        return Result.success(log);
    }

    @ApiOperation(value = "清空日志")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping
    public Result<Void> clear(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Boolean confirm) {
        // 安全检查：清空全部日志时需要确认参数
        if (startTime == null && endTime == null) {
            if (confirm == null || !confirm) {
                throw new BusinessException("清空全部日志是危险操作，请传入confirm=true参数进行确认");
            }
        }
        logService.clearLogs(startTime, endTime);
        return Result.success();
    }

    @ApiOperation(value = "导出日志")
    @PreAuthorize("hasRole('admin')")
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
