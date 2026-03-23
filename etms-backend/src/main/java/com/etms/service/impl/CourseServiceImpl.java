package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Course;
import com.etms.entity.PlanCourse;
import com.etms.exception.BusinessException;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.PlanCourseMapper;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    
    private final PlanCourseMapper planCourseMapper;
    
    @Override
    public Page<CourseVO> pageCourses(Page<Course> page, String courseName, String courseCode, Long categoryId, Integer courseType, Integer status, Integer difficulty) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(courseName), Course::getCourseName, courseName)
               .like(StringUtils.hasText(courseCode), Course::getCourseCode, courseCode)
               .eq(categoryId != null, Course::getCategoryId, categoryId)
               .eq(courseType != null, Course::getCourseType, courseType)
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
            vo.setCourseTypeName(getCourseTypeName(course.getCourseType()));
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
        vo.setCourseTypeName(getCourseTypeName(course.getCourseType()));
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
    @Transactional(rollbackFor = Exception.class)
    public boolean addCourse(Course course) {
        // 检查课程编码是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Course>().eq(Course::getCourseCode, course.getCourseCode())
        );
        if (count > 0) {
            throw new BusinessException("课程编码已存在");
        }
        
        course.setStatus(0); // 草稿状态
        course.setViewCount(0);
        course.setCollectCount(0);
        course.setRatingCount(0);
        return baseMapper.insert(course) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCourse(Course course) {
        // 检查课程编码是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, course.getCourseCode())
                .ne(Course::getId, course.getId())
        );
        if (count > 0) {
            throw new BusinessException("课程编码已存在");
        }
        
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long id) {
        // 检查课程是否被培训计划引用
        Long count = planCourseMapper.selectCount(
            new LambdaQueryWrapper<PlanCourse>().eq(PlanCourse::getCourseId, id)
        );
        if (count > 0) {
            throw new BusinessException("课程已被培训计划引用，无法删除");
        }
        
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAudit(Long id) {
        // 获取当前课程状态
        Course existingCourse = baseMapper.selectById(id);
        if (existingCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 校验状态流转：只能从草稿(0)或审核驳回(4)状态提交审核
        if (existingCourse.getStatus() != 0 && existingCourse.getStatus() != 4) {
            throw new BusinessException("当前状态不允许提交审核，只有草稿或审核驳回状态可以提交");
        }
        
        Course course = new Course();
        course.setId(id);
        course.setStatus(1); // 待审核
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditCourse(Long id, Integer status, String auditRemark) {
        // 获取当前课程状态
        Course existingCourse = baseMapper.selectById(id);
        if (existingCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 校验状态流转：只能审核待审核(1)状态的课程
        if (existingCourse.getStatus() != 1) {
            throw new BusinessException("当前状态不允许审核，只有待审核状态可以审核");
        }
        
        // 校验审核状态值：审核通过(2)或审核驳回(4)
        if (status != 2 && status != 4) {
            throw new BusinessException("审核状态无效，审核通过状态为2，审核驳回状态为4");
        }
        
        Course course = new Course();
        course.setId(id);
        course.setStatus(status);
        course.setAuditRemark(auditRemark);
        course.setAuditTime(LocalDateTime.now());
        // course.setAuditBy(getCurrentUserId());
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishCourse(Long id) {
        // 获取当前课程状态
        Course existingCourse = baseMapper.selectById(id);
        if (existingCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 校验状态流转：只允许已下架(3)状态的课程重新上架
        // 审核通过(2)的课程已经是已上架状态，不需要再上架
        if (existingCourse.getStatus() != 3) {
            throw new BusinessException("当前状态不允许上架操作，只有已下架状态的课程可以上架");
        }
        
        Course course = new Course();
        course.setId(id);
        course.setStatus(2); // 已上架
        return baseMapper.updateById(course) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unpublishCourse(Long id) {
        // 获取当前课程状态
        Course existingCourse = baseMapper.selectById(id);
        if (existingCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 校验状态流转：只能从已上架(2)状态下架
        if (existingCourse.getStatus() != 2) {
            throw new BusinessException("当前状态不允许下架操作，只有已上架状态可以下架");
        }
        
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
    
    private String getCourseTypeName(Integer courseType) {
        if (courseType == null) return "未知";
        switch (courseType) {
            case 1: return "视频";
            case 2: return "文档";
            case 3: return "直播";
            default: return "未知";
        }
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
