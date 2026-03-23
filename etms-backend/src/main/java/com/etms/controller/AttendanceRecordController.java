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
    
    @ApiOperation(value = "签到/签退")
    @PostMapping("/sign")
    public Result<Void> signIn(@RequestBody Map<String, Object> body) {
        // 参数验证
        if (body.get("planId") == null) {
            return Result.error("培训计划ID不能为空");
        }

        Long planId = Long.valueOf(body.get("planId").toString());
        Integer signType = body.get("signType") != null ? Integer.valueOf(body.get("signType").toString()) : 1;
        Integer signCategory = body.get("signCategory") != null ? Integer.valueOf(body.get("signCategory").toString()) : 1;
        String location = body.get("location") != null ? body.get("location").toString() : null;
        
        // 修复：验证签到类型值是否合法（1: 二维码, 2: GPS定位, 3: 人脸识别）
        if (signType < 1 || signType > 3) {
            return Result.error("签到类型不合法，有效值为1-3");
        }
        // 修复：验证签到类别值是否合法（1: 签到, 2: 签退）
        if (signCategory < 1 || signCategory > 2) {
            return Result.error("签到类别不合法，有效值为1-2");
        }
        
        attendanceRecordService.signIn(planId, signType, signCategory, location);
        return Result.success();
    }
    
    @ApiOperation(value = "补签申请")
    @PostMapping("/supplementary")
    public Result<Void> applySupplementary(@RequestBody Map<String, Object> body) {
        // 参数验证
        if (body.get("planId") == null) {
            return Result.error("培训计划ID不能为空");
        }
        if (body.get("signTime") == null) {
            return Result.error("签到时间不能为空");
        }

        Long planId = Long.valueOf(body.get("planId").toString());
        Integer signType = body.get("signType") != null ? Integer.valueOf(body.get("signType").toString()) : 1;
        Integer signCategory = body.get("signCategory") != null ? Integer.valueOf(body.get("signCategory").toString()) : 1;
        String signTime = body.get("signTime").toString();
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        
        // 修复：验证签到类型值是否合法（1: 二维码, 2: GPS定位, 3: 人脸识别）
        if (signType < 1 || signType > 3) {
            return Result.error("签到类型不合法，有效值为1-3");
        }
        // 修复：验证签到类别值是否合法（1: 签到, 2: 签退）
        if (signCategory < 1 || signCategory > 2) {
            return Result.error("签到类别不合法，有效值为1-2");
        }
        
        attendanceRecordService.applySupplementary(planId, signType, signCategory, signTime, reason);
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
        // 参数验证
        if (body.get("auditStatus") == null) {
            return Result.error("审核状态不能为空");
        }

        Integer auditStatus = Integer.valueOf(body.get("auditStatus").toString());
        // 验证审核状态值是否合法（1: 通过, 2: 驳回）
        if (auditStatus != 1 && auditStatus != 2) {
            return Result.error("审核状态值不合法");
        }

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
