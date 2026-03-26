package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.SignInDTO;
import com.etms.dto.SupplementaryDTO;
import com.etms.dto.AttendanceAuditDTO;
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
import javax.validation.Valid;

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
            @RequestParam(defaultValue = "1") @javax.validation.constraints.Min(value = 1, message = "页码最小为1") Long current,
            @RequestParam(defaultValue = "10") @javax.validation.constraints.Min(value = 1, message = "每页条数最小为1") @javax.validation.constraints.Max(value = 100, message = "每页条数最大为100") Long size,
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
    @PreAuthorize("isAuthenticated()")
    public Result<Void> signIn(@Valid @RequestBody SignInDTO signInDTO, HttpServletRequest request) {
        // 修复问题5：获取客户端IP和设备信息
        String ipAddress = getClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        
        attendanceRecordService.signIn(
            signInDTO.getPlanId(), 
            signInDTO.getSignType(), 
            signInDTO.getSignCategory(), 
            signInDTO.getLocation(), 
            ipAddress, 
            deviceInfo
        );
        return Result.success();
    }
    
    @ApiOperation(value = "补签申请")
    @PostMapping("/supplementary")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> applySupplementary(@Valid @RequestBody SupplementaryDTO supplementaryDTO) {
        attendanceRecordService.applySupplementary(
            supplementaryDTO.getPlanId(), 
            supplementaryDTO.getSignType(), 
            supplementaryDTO.getSignCategory(), 
            supplementaryDTO.getSignTime(), 
            supplementaryDTO.getReason()
        );
        return Result.success();
    }
    
    @ApiOperation(value = "撤销补签申请")
    @DeleteMapping("/supplementary/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelSupplementary(@PathVariable Long id) {
        // 修复：添加权限验证，确保用户只能撤销自己的补签申请
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        // 验证补签记录是否属于当前用户
        if (!attendanceRecordService.isOwner(id, currentUser.getId())) {
            return Result.error("无权撤销他人的补签申请");
        }
        
        attendanceRecordService.cancelSupplementary(id);
        return Result.success();
    }
    
    @ApiOperation(value = "补签审核")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AttendanceAuditDTO auditDTO) {
        attendanceRecordService.auditSupplement(id, auditDTO.getAuditStatus(), auditDTO.getAuditRemark());
        return Result.success();
    }
    
    @ApiOperation(value = "获取个人签到统计")
    @GetMapping("/stats/personal")
    @PreAuthorize("isAuthenticated()")
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
