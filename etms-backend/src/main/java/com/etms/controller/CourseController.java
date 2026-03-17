package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Course;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 课程控制器
 */
@Tag(name = "课程管理")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    
    private final CourseService courseService;
    
    @Operation(summary = "分页查询课程列表")
    @GetMapping
    public Result<PageResult<CourseVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer difficulty) {
        Page<Course> page = new Page<>(current, size);
        Page<CourseVO> voPage = courseService.pageCourses(page, courseName, categoryId, status, difficulty);
        PageResult<CourseVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseVO> get(@PathVariable Long id) {
        CourseVO vo = courseService.getCourseDetail(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增课程")
    @PostMapping
    public Result<Void> add(@RequestBody Course course) {
        courseService.addCourse(course);
        return Result.success();
    }
    
    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        courseService.updateCourse(course);
        return Result.success();
    }
    
    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
    
    @Operation(summary = "提交审核")
    @PostMapping("/{id}/submit")
    public Result<Void> submitAudit(@PathVariable Long id) {
        courseService.submitAudit(id);
        return Result.success();
    }
    
    @Operation(summary = "审核课程")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditRemark) {
        courseService.auditCourse(id, status, auditRemark);
        return Result.success();
    }
    
    @Operation(summary = "上架课程")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publishCourse(id);
        return Result.success();
    }
    
    @Operation(summary = "下架课程")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublishCourse(id);
        return Result.success();
    }
    
    @Operation(summary = "获取课程列表(不分页)")
    @GetMapping("/list")
    public Result<List<CourseVO>> list(@RequestParam(required = false) Long categoryId) {
        List<CourseVO> list = courseService.listCourses(categoryId);
        return Result.success(list);
    }
}
