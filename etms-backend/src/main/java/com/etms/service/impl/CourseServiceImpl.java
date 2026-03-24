package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Course;
import com.etms.entity.Paper;
import com.etms.entity.Question;
import com.etms.entity.PlanCourse;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.mapper.PlanCourseMapper;
import com.etms.mapper.UserMapper;
import com.etms.service.CourseService;
import com.etms.vo.CourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PaperMapper paperMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;
    
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
        
        // 修复并发问题：使用数据库原子操作更新浏览次数
        baseMapper.incrementViewCount(id);
        
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
        // 获取现有课程信息
        Course existingCourse = baseMapper.selectById(course.getId());
        if (existingCourse == null) {
            throw new BusinessException("课程不存在");
        }
        
        // 已上架(2)状态的课程不能随意编辑
        if (existingCourse.getStatus() == 2) {
            throw new BusinessException("已上架的课程不能编辑，请先下架后再修改");
        }
        
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
        Long planCount = planCourseMapper.selectCount(
            new LambdaQueryWrapper<PlanCourse>().eq(PlanCourse::getCourseId, id)
        );
        if (planCount > 0) {
            throw new BusinessException("课程已被培训计划引用，无法删除");
        }
        
        // 修复问题9：检查课程是否被试卷引用
        Long paperCount = paperMapper.selectCount(
            new LambdaQueryWrapper<Paper>().eq(Paper::getCourseId, id)
        );
        if (paperCount > 0) {
            throw new BusinessException("课程已被试卷引用，无法删除。请先删除相关试卷。");
        }
        
        // 修复问题9：检查课程是否被题目引用
        Long questionCount = questionMapper.selectCount(
            new LambdaQueryWrapper<Question>().eq(Question::getCourseId, id)
        );
        if (questionCount > 0) {
            throw new BusinessException("课程已被题目引用，无法删除。请先删除相关题目。");
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
        
        // 验证必要字段：课程名称
        if (existingCourse.getCourseName() == null || existingCourse.getCourseName().trim().isEmpty()) {
            throw new BusinessException("请先设置课程名称");
        }
        
        // 验证必要字段：课程类型
        if (existingCourse.getCourseType() == null) {
            throw new BusinessException("请先设置课程类型");
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
        // 修复：记录审核人ID
        Long auditBy = getCurrentUserId();
        if (auditBy != null) {
            course.setAuditBy(auditBy);
        }
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
        
        // 修复：完善状态检查逻辑，明确各状态下的操作限制
        // 课程状态定义：0-草稿，1-待审核，2-已上架，3-已下架，4-审核驳回
        switch (existingCourse.getStatus()) {
            case 0: // 草稿
                throw new BusinessException("草稿状态的课程请先提交审核");
            case 1: // 待审核
                throw new BusinessException("课程正在审核中，请等待审核完成");
            case 2: // 已上架
                throw new BusinessException("课程已上架，无需重复操作");
            case 3: // 已下架
                // 修复安全问题：已下架的课程需要重新提交审核后才能上架
                throw new BusinessException("已下架的课程需要重新提交审核后才能上架");
            case 4: // 审核驳回
                throw new BusinessException("课程审核未通过，请修改后重新提交审核");
            default:
                throw new BusinessException("课程状态异常，无法上架");
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
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 从认证信息中获取用户名，再查询用户ID
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        // 通过用户名查询用户ID
        User user = userMapper.selectByUsername(username);
        return user != null ? user.getId() : null;
    }
}
