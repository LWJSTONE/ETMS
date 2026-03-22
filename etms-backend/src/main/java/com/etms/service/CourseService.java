package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Course;
import com.etms.vo.CourseVO;
import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {
    
    /**
     * 分页查询课程列表
     */
    Page<CourseVO> pageCourses(Page<Course> page, String courseName, String courseCode, Long categoryId, Integer courseType, Integer status, Integer difficulty);
    
    /**
     * 获取课程详情
     */
    CourseVO getCourseDetail(Long id);
    
    /**
     * 新增课程
     */
    boolean addCourse(Course course);
    
    /**
     * 更新课程
     */
    boolean updateCourse(Course course);
    
    /**
     * 删除课程
     */
    boolean deleteCourse(Long id);
    
    /**
     * 提交审核
     */
    boolean submitAudit(Long id);
    
    /**
     * 审核课程
     */
    boolean auditCourse(Long id, Integer status, String auditRemark);
    
    /**
     * 上架课程
     */
    boolean publishCourse(Long id);
    
    /**
     * 下架课程
     */
    boolean unpublishCourse(Long id);
    
    /**
     * 获取课程列表(不分页)
     */
    List<CourseVO> listCourses(Long categoryId);
}
