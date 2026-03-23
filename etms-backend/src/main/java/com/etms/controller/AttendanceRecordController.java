package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.AttendanceRecord;
import com.etms.service.AttendanceRecordService;
import com.etms.vo.AttendanceRecordVO;
import com.etms.service.UserService;
import com.etms.entity.User;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 签到记录控制器
 */
@Api(tags = "签到管理")
@RestController
@RequestMapping("/attendance/records")
@RequiredArgsConstructor
@Validated
public class AttendanceRecordController {
    
    private final AttendanceRecordService attendanceRecordService;
    private final UserService userService;
    
    @ApiOperation(value = "分页查询签到记录")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
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
    public Result<Void> signIn(@RequestBody Map<String, Object> body, HttpServletRequest request) {
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
        
        // 修复问题5：获取客户端IP和设备信息
        String ipAddress = getClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        
        attendanceRecordService.signIn(planId, signType, signCategory, location, ipAddress, deviceInfo);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
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
    @GetMapping("/stats/personal")
    public Result<?> getPersonalStats() {
        // 修复越权问题：只能查询自己的统计
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        return Result.success(attendanceRecordService.getPersonalStats(currentUser.getId()));
    }
    
    @ApiOperation(value = "获取指定用户签到统计(管理员)")
    @GetMapping("/stats/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<?> getUserStats(@PathVariable Long userId) {
        return Result.success(attendanceRecordService.getPersonalStats(userId));
    }
    
    /**
     * 获取客户端真实IP地址
     * 修复问题5：支持从代理服务器获取真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多次代理的情况，取第一个非unknown的IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
