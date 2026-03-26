package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.SupplementaryDTO;
import com.etms.dto.AttendanceAuditDTO;
import com.etms.entity.AttendanceRecord;
import com.etms.entity.User;
import com.etms.service.AttendanceRecordService;
import com.etms.service.UserService;
import com.etms.vo.AttendanceRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 考勤申请控制器
 * 用于处理补签申请等考勤相关操作
 */
@Api(tags = "考勤申请管理")
@RestController
@RequestMapping("/attendance/apply")
@RequiredArgsConstructor
@Validated
public class AttendanceApplyController {
    
    private final AttendanceRecordService attendanceRecordService;
    private final UserService userService;
    
    @ApiOperation(value = "分页查询补签申请列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<PageResult<AttendanceRecordVO>> page(
            @RequestParam(defaultValue = "1") @javax.validation.constraints.Min(value = 1, message = "页码最小为1") Long current,
            @RequestParam(defaultValue = "10") @javax.validation.constraints.Min(value = 1, message = "每页条数最小为1") @javax.validation.constraints.Max(value = 100, message = "每页条数最大为100") Long size,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer auditStatus) {
        
        Page<AttendanceRecord> page = new Page<>(current, size);
        
        // 根据用户名获取用户ID
        Long userId = null;
        if (username != null && !username.isEmpty()) {
            User user = userService.getByUsername(username);
            if (user != null) {
                userId = user.getId();
            }
        }
        
        // 查询补签申请记录（status = 5 表示补签）
        Page<AttendanceRecordVO> voPage = attendanceRecordService.pageRecords(
            page, planId, userId, 5, auditStatus
        );
        
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "提交补签申请")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> apply(@Valid @RequestBody SupplementaryDTO supplementaryDTO) {
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
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancel(@PathVariable Long id) {
        // 获取当前用户
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
    
    @ApiOperation(value = "审核补签申请")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody AttendanceAuditDTO auditDTO) {
        attendanceRecordService.auditSupplement(id, auditDTO.getAuditStatus(), auditDTO.getAuditRemark());
        return Result.success();
    }
    
    @ApiOperation(value = "获取我的补签申请列表")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<AttendanceRecordVO>> myApplies(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
            @RequestParam(required = false) Integer auditStatus) {
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return Result.error("用户未登录");
        }
        
        Page<AttendanceRecord> page = new Page<>(current, size);
        
        // 查询当前用户的补签申请记录
        Page<AttendanceRecordVO> voPage = attendanceRecordService.pageRecords(
            page, null, currentUser.getId(), 5, auditStatus
        );
        
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "根据用户名查询补签申请")
    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER', 'DEPT_MANAGER')")
    public Result<PageResult<AttendanceRecordVO>> getByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
            @RequestParam(required = false) Integer auditStatus) {
        
        // 根据用户名获取用户
        User user = userService.getByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Page<AttendanceRecord> page = new Page<>(current, size);
        
        // 查询该用户的补签申请记录
        Page<AttendanceRecordVO> voPage = attendanceRecordService.pageRecords(
            page, null, user.getId(), 5, auditStatus
        );
        
        PageResult<AttendanceRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
}
