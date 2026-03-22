package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.AttendanceRecord;
import com.etms.service.AttendanceRecordService;
import com.etms.vo.AttendanceRecordVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 签到记录控制器
 */
@Api(tags = "签到管理")
@RestController
@RequestMapping("/attendance/records")
@RequiredArgsConstructor
public class AttendanceRecordController {
    
    private final AttendanceRecordService attendanceRecordService;
    
    @ApiOperation(value = "分页查询签到记录")
    @GetMapping
    public Result<PageResult<AttendanceRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer auditStatus) {
        Page<AttendanceRecord> page = new Page<>(current, size);
        Page<AttendanceRecordVO> voPage = attendanceRecordService.pageRecords(page, planId, userId, status, auditStatus);
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "签到")
    @PostMapping("/sign")
    public Result<Void> signIn(@RequestBody Map<String, Object> body) {
        Long planId = Long.valueOf(body.get("planId").toString());
        Integer signType = Integer.valueOf(body.get("signType").toString());
        String location = body.get("location") != null ? body.get("location").toString() : null;
        attendanceRecordService.signIn(planId, signType, location);
        return Result.success();
    }
    
    @ApiOperation(value = "补签申请")
    @PostMapping("/supplementary")
    public Result<Void> applySupplementary(@RequestBody Map<String, Object> body) {
        Long planId = Long.valueOf(body.get("planId").toString());
        Integer signType = Integer.valueOf(body.get("signType").toString());
        String signTime = body.get("signTime") != null ? body.get("signTime").toString() : null;
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        attendanceRecordService.applySupplementary(planId, signType, signTime, reason);
        return Result.success();
    }
    
    @ApiOperation(value = "撤销补签申请")
    @DeleteMapping("/supplementary/{id}")
    public Result<Void> cancelSupplementary(@PathVariable Long id) {
        attendanceRecordService.cancelSupplementary(id);
        return Result.success();
    }
    
    @ApiOperation(value = "补签审核")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        String auditRemark = body.get("auditRemark") != null ? body.get("auditRemark").toString() : null;
        attendanceRecordService.auditSupplement(id, auditStatus, auditRemark);
        return Result.success();
    }
    
    @ApiOperation(value = "获取个人签到统计")
    @GetMapping("/stats/{userId}")
    public Result<?> getPersonalStats(@PathVariable Long userId) {
        return Result.success(attendanceRecordService.getPersonalStats(userId));
    }
}
