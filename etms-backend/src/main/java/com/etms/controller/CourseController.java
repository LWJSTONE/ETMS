package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Course;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
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
    public Result<PageResult<CourseVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer difficulty) {
        Page<Course> page = new Page<>(current, size);
        Page<CourseVO> voPage = courseService.pageCourses(page, courseName, courseCode, categoryId, courseType, status, difficulty);
        PageResult<CourseVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取课程详情")
    @GetMapping("/{id}")
    public Result<CourseVO> get(@PathVariable Long id) {
        CourseVO vo = courseService.getCourseDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增课程")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Course course) {
        courseService.addCourse(course);
        return Result.success();
    }
    
    @ApiOperation(value = "更新课程")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Course course) {
        course.setId(id);
        courseService.updateCourse(course);
        return Result.success();
    }
    
    @ApiOperation(value = "删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "提交审核")
    @PostMapping("/{id}/submit")
    public Result<Void> submitAudit(@PathVariable Long id) {
        courseService.submitAudit(id);
        return Result.success();
    }
    
    @ApiOperation(value = "审核课程")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditRemark) {
        courseService.auditCourse(id, status, auditRemark);
        return Result.success();
    }
    
    @ApiOperation(value = "上架课程")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        courseService.publishCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "下架课程")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublish(@PathVariable Long id) {
        courseService.unpublishCourse(id);
        return Result.success();
    }
    
    @ApiOperation(value = "获取课程列表(不分页)")
    @GetMapping("/all")
    public Result<List<CourseVO>> list(@RequestParam(required = false) Long categoryId) {
        List<CourseVO> list = courseService.listCourses(categoryId);
        return Result.success(list);
    }
}
