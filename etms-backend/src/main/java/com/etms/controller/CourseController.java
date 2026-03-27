package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.CourseAuditDTO;
import com.etms.entity.Course;
import com.etms.exception.BusinessException;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 课程控制器
 */
@Api(tags = "课程管理")
@RestController
@RequestMapping("/training/courses")
@RequiredArgsConstructor
@Validated
public class CourseController {
    
    private final CourseService courseService;
    
    @ApiOperation(value = "分页查询课程列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<CourseVO>> page(
            @RequestParam(defaultValue = "1") @javax.validation.constraints.Min(value = 1, message = "页码最小为1") Long current,
            @RequestParam(defaultValue = "10") @javax.validation.constraints.Min(value = 1, message = "每页条数最小为1") @javax.validation.constraints.Max(value = 100, message = "每页条数最大为100") Long size,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer difficulty) {
        // 修复：根据用户权限过滤课程数据
        // 未登录或普通用户只能查看已发布的课程
        // 管理员可以查看所有课程
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_admin".equals(a.getAuthority()) || 
                          "ROLE_train_admin".equals(a.getAuthority()));
        
        // 如果不是管理员，强制只显示已上架的课程(状态2表示已上架)
        Integer queryStatus = status;
        if (!isAdmin) {
            queryStatus = 2; // 只显示已上架的课程
        }
        
        Page<Course> page = new Page<>(current, size);
        Page<CourseVO> voPage = courseService.pageCourses(page, courseName, courseCode, categoryId, courseType, queryStatus, difficulty);
        PageResult<CourseVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取课程详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制，确保只有登录用户可访问
    public Result<CourseVO> get(@PathVariable Long id) {
        CourseVO vo = courseService.getCourseDetail(id);
        // 修复：课程不存在时抛出业务异常
        if (vo == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 修复：权限校验 - 非管理员只能查看已上架的课程
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_admin".equals(a.getAuthority()) || 
                          "ROLE_train_admin".equals(a.getAuthority()));
        
        if (!isAdmin && vo.getStatus() != null && vo.getStatus() != 2) {
            throw new BusinessException("您无权查看该课程");
        }
        
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增课程")
    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> add(@Valid @RequestBody Course course) {
        courseService.addCourse(course);
        return Result.success();
    }
    
    @ApiOperation(value = "更新课程")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Course course) {
        course.setId(id);
        courseService.updateCourse(course);
        return Result.success();
    }
    
    @ApiOperation(value = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> delete(@PathVariable Long id) {
        // 修复：删除课程前检查是否有关联的培训计划，统一使用BusinessException
        if (courseService.hasTrainingPlans(id)) {
            throw new BusinessException("该课程存在关联的培训计划，无法删除");
        }
        courseService.deleteCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "提交审核")
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> submitAudit(@PathVariable Long id) {
        courseService.submitAudit(id);
        return Result.success();
    }
    
    @ApiOperation(value = "审核课程")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> audit(
            @PathVariable Long id,
            @Valid @RequestBody CourseAuditDTO auditDTO) {
        courseService.auditCourse(id, auditDTO.getStatus(), auditDTO.getAuditRemark());
        return Result.success();
    }
    
    @ApiOperation(value = "上架课程")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publishCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "下架课程")
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('admin', 'train_admin')")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublishCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "获取课程列表(不分页)")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public Result<List<CourseVO>> list(@RequestParam(required = false) Long categoryId) {
        // 修复：根据用户权限过滤课程数据
        // 未登录或普通用户只能查看已发布的课程
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_admin".equals(a.getAuthority()) || 
                          "ROLE_train_admin".equals(a.getAuthority()));
        
        List<CourseVO> list = courseService.listCourses(categoryId);
        
        // 修复：课程状态判断不一致问题
        // 统一使用状态2表示已上架/已发布的课程（与page()方法保持一致）
        if (!isAdmin) {
            list = list.stream()
                .filter(c -> c.getStatus() != null && c.getStatus() == 2)
                .collect(java.util.stream.Collectors.toList());
        }
        
        return Result.success(list);
    }
}
