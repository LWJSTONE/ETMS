package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.AttendanceRecord;
import com.etms.service.AttendanceRecordService;
import com.etms.vo.AttendanceRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 签到记录控制器
 */
@Tag(name = "签到管理")
@RestController
@RequestMapping("/attendance/records")
@RequiredArgsConstructor
public class AttendanceRecordController {
    
    private final AttendanceRecordService attendanceRecordService;
    
    @Operation(summary = "分页查询签到记录")
    @GetMapping
    public Result<PageResult<AttendanceRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        Page<AttendanceRecord> page = new Page<>(current, size);
        Page<AttendanceRecordVO> voPage = attendanceRecordService.pageRecords(page, planId, userId, status);
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @Operation(summary = "签到")
    @PostMapping("/sign")
    public Result<Void> signIn(
            @RequestParam Long planId,
            @RequestParam Integer signType,
            @RequestParam(required = false) String location) {
        attendanceRecordService.signIn(planId, signType, location);
        return Result.success();
    }
    
    @Operation(summary = "补签审核")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditRemark) {
        attendanceRecordService.auditSupplement(id, auditStatus, auditRemark);
        return Result.success();
    }
    
    @Operation(summary = "获取个人签到统计")
    @GetMapping("/stats/{userId}")
    public Result<?> getPersonalStats(@PathVariable Long userId) {
        return Result.success(attendanceRecordService.getPersonalStats(userId));
    }
}
