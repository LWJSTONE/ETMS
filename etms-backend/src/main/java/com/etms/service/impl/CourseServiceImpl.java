package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Course;
import com.etms.mapper.CourseMapper;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    
    @Override
    public Page<CourseVO> pageCourses(Page<Course> page, String courseName, Long categoryId, Integer status, Integer difficulty) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(courseName), Course::getCourseName, courseName)
               .eq(categoryId != null, Course::getCategoryId, categoryId)
               .eq(status != null, Course::getStatus, status)
               .eq(difficulty != null, Course::getDifficulty, difficulty)
               .orderByDesc(Course::getCreateTime);
        
        Page<Course> coursePage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<CourseVO> voPage = new Page<>();
        BeanUtils.copyProperties(coursePage, voPage, "records");
        
        List<CourseVO> voList = coursePage.getRecords().stream().map(course -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(course, vo);
            vo.setDifficultyName(getDifficultyName(course.getDifficulty()));
            vo.setStatusName(getStatusName(course.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public CourseVO getCourseDetail(Long id) {
        Course course = baseMapper.selectById(id);
        if (course == null) {
            return null;
        }
        
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        vo.setDifficultyName(getDifficultyName(course.getDifficulty()));
        vo.setStatusName(getStatusName(course.getStatus()));
        
        // 更新浏览次数
        Course updateCourse = new Course();
        updateCourse.setId(id);
        updateCourse.setViewCount(course.getViewCount() + 1);
        baseMapper.updateById(updateCourse);
        
        return vo;
    }
    
    @Override
    public boolean addCourse(Course course) {
        course.setStatus(0); // 草稿状态
        course.setViewCount(0);
        course.setCollectCount(0);
        course.setRatingCount(0);
        return baseMapper.insert(course) > 0;
    }
    
    @Override
    public boolean updateCourse(Course course) {
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    public boolean deleteCourse(Long id) {
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean submitAudit(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(1); // 待审核
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    public boolean auditCourse(Long id, Integer status, String auditRemark) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        course.setAuditRemark(auditRemark);
        course.setAuditTime(LocalDateTime.now());
        // course.setAuditBy(getCurrentUserId());
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    public boolean publishCourse(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(2); // 已上架
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    public boolean unpublishCourse(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setStatus(3); // 已下架
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    public List<CourseVO> listCourses(Long categoryId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, 2) // 已上架
               .eq(categoryId != null, Course::getCategoryId, categoryId)
               .orderByDesc(Course::getCreateTime);
        
        List<Course> courses = baseMapper.selectList(wrapper);
        
        return courses.stream().map(course -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(course, vo);
            vo.setDifficultyName(getDifficultyName(course.getDifficulty()));
            vo.setStatusName(getStatusName(course.getStatus()));
            return vo;
        }).collect(Collectors.toList());
    }
    
    private String getDifficultyName(Integer difficulty) {
        if (difficulty == null) return "未知";
        switch (difficulty) {
            case 1: return "入门";
            case 2: return "初级";
            case 3: return "中级";
            case 4: return "高级";
            case 5: return "专家";
            default: return "未知";
        }
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "待审核";
            case 2: return "已上架";
            case 3: return "已下架";
            case 4: return "审核驳回";
            default: return "未知";
        }
    }
}
