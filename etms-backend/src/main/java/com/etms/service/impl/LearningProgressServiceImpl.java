package com.etms.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.User;
import com.etms.entity.UserPlan;
import com.etms.entity.TrainingPlan;
import com.etms.entity.Course;
import com.etms.entity.Dept;
import com.etms.exception.BusinessException;
import com.etms.mapper.UserPlanMapper;
import com.etms.mapper.UserMapper;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.DeptMapper;
import com.etms.service.LearningProgressService;
import com.etms.vo.LearningProgressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习进度服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningProgressServiceImpl extends ServiceImpl<UserPlanMapper, UserPlan> implements LearningProgressService {
    
    private final UserMapper userMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    private final CourseMapper courseMapper;
    private final DeptMapper deptMapper;
    
    @Override
    public Page<LearningProgressVO> pageProgress(Long current, Long size, Long planId, Long userId, Integer status, String userName, String planName, String sortField, String sortOrder) {
        Page<UserPlan> page = new Page<>(current, size);
        
        LambdaQueryWrapper<UserPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, UserPlan::getPlanId, planId)
               .eq(userId != null, UserPlan::getUserId, userId)
               .eq(status != null, UserPlan::getStatus, status);
        
        // 处理排序参数
        if (sortField != null && !sortField.isEmpty() && sortOrder != null && !sortOrder.isEmpty()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "progress":
                    wrapper.orderBy(true, isAsc, UserPlan::getProgress);
                    break;
                case "createTime":
                    wrapper.orderBy(true, isAsc, UserPlan::getCreateTime);
                    break;
                case "lastStudyTime":
                    wrapper.orderBy(true, isAsc, UserPlan::getLastStudyTime);
                    break;
                case "updateTime":
                    wrapper.orderBy(true, isAsc, UserPlan::getUpdateTime);
                    break;
                default:
                    wrapper.orderByDesc(UserPlan::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(UserPlan::getCreateTime);
        }
        
        // 修复：将用户名和计划名的过滤改为数据库查询阶段完成，避免先分页后内存过滤导致数据不准确
        // 根据用户名查询符合条件的用户ID列表
        if (userName != null && !userName.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(User::getRealName, userName)
                      .or()
                      .like(User::getUsername, userName);
            List<User> matchedUsers = userMapper.selectList(userWrapper);
            if (matchedUsers.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Page<LearningProgressVO> emptyVoPage = new Page<>(current, size, 0);
                emptyVoPage.setRecords(new ArrayList<>());
                return emptyVoPage;
            }
            List<Long> matchedUserIds = matchedUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            wrapper.in(UserPlan::getUserId, matchedUserIds);
        }
        
        // 根据计划名查询符合条件的计划ID列表
        if (planName != null && !planName.isEmpty()) {
            LambdaQueryWrapper<TrainingPlan> planWrapper = new LambdaQueryWrapper<>();
            planWrapper.like(TrainingPlan::getPlanName, planName);
            List<TrainingPlan> matchedPlans = trainingPlanMapper.selectList(planWrapper);
            if (matchedPlans.isEmpty()) {
                // 没有匹配的计划，返回空结果
                Page<LearningProgressVO> emptyVoPage = new Page<>(current, size, 0);
                emptyVoPage.setRecords(new ArrayList<>());
                return emptyVoPage;
            }
            List<Long> matchedPlanIds = matchedPlans.stream()
                    .map(TrainingPlan::getId)
                    .collect(Collectors.toList());
            wrapper.in(UserPlan::getPlanId, matchedPlanIds);
        }
        
        Page<UserPlan> userPlanPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<LearningProgressVO> voPage = new Page<>(current, size, userPlanPage.getTotal());
        
        if (userPlanPage.getRecords().isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }
        
        // 批量获取用户信息
        Set<Long> userIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            
            // 批量获取部门名称
            Set<Long> deptIds = users.stream()
                    .map(User::getDeptId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!deptIds.isEmpty()) {
                List<Dept> depts = deptMapper.selectBatchIds(deptIds);
                deptNameMap = depts.stream().collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
            }
        }
        
        // 批量获取培训计划信息
        Set<Long> planIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getPlanId)
                .collect(Collectors.toSet());
        Map<Long, TrainingPlan> planMap = new HashMap<>();
        if (!planIds.isEmpty()) {
            List<TrainingPlan> plans = trainingPlanMapper.selectBatchIds(planIds);
            planMap = plans.stream().collect(Collectors.toMap(TrainingPlan::getId, p -> p));
        }
        
        // 批量获取课程信息
        Set<Long> courseIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 从培训计划中获取关联的课程ID
        for (TrainingPlan plan : planMap.values()) {
            if (plan.getCourseId() != null) {
                courseIds.add(plan.getCourseId());
            }
        }
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseMap = courses.stream().collect(Collectors.toMap(Course::getId, c -> c));
        }
        
        // 转换为VO（不再需要内存过滤）
        final Map<Long, User> finalUserMap = userMap;
        final Map<Long, String> finalDeptNameMap = deptNameMap;
        final Map<Long, TrainingPlan> finalPlanMap = planMap;
        final Map<Long, Course> finalCourseMap = courseMap;
        
        List<LearningProgressVO> voList = userPlanPage.getRecords().stream()
                .map(up -> {
                    LearningProgressVO vo = new LearningProgressVO();
                    BeanUtils.copyProperties(up, vo);
                    
                    // 设置用户信息
                    User user = finalUserMap.get(up.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getUsername());
                        vo.setRealName(user.getRealName());
                        vo.setDeptName(finalDeptNameMap.get(user.getDeptId()));
                    }
                    
                    // 设置培训计划信息
                    TrainingPlan plan = finalPlanMap.get(up.getPlanId());
                    if (plan != null) {
                        vo.setPlanName(plan.getPlanName());
                    }
                    
                    // 设置课程信息
                    Long courseId = up.getCourseId() != null ? up.getCourseId() : 
                                   (plan != null ? plan.getCourseId() : null);
                    if (courseId != null) {
                        Course course = finalCourseMap.get(courseId);
                        if (course != null) {
                            vo.setCourseId(courseId);
                            vo.setCourseName(course.getCourseName());
                            vo.setCourseType(course.getCourseType());
                            vo.setCoverImage(course.getCoverImage());
                            vo.setCourseDesc(course.getCourseDesc());
                            vo.setDuration(course.getDuration());
                            vo.setCredit(course.getCredit());
                        }
                    }
                    
                    // 设置状态名称
                    vo.setStatusName(getStatusName(up.getStatus()));
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public Page<LearningProgressVO> getMyProgress(Long current, Long size, Integer status, String keyword, Long planId) {
        // 获取当前用户ID
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        String username = auth.getName();
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return pageProgressWithKeyword(current, size, planId, user.getId(), status, keyword);
    }
    
    /**
     * 支持课程名称关键字过滤的分页查询
     * 修复：将课程名称过滤改为数据库查询阶段完成，避免内存过滤导致分页数据不准确
     */
    private Page<LearningProgressVO> pageProgressWithKeyword(Long current, Long size, Long planId, Long userId, Integer status, String keyword) {
        Page<UserPlan> page = new Page<>(current, size);
        
        LambdaQueryWrapper<UserPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, UserPlan::getPlanId, planId)
               .eq(userId != null, UserPlan::getUserId, userId)
               .eq(status != null, UserPlan::getStatus, status)
               .orderByDesc(UserPlan::getCreateTime);
        
        // 修复：在数据库层面过滤课程名称关键字，避免先分页后内存过滤导致数据不准确
        if (keyword != null && !keyword.isEmpty()) {
            // 根据课程名称查询符合条件的课程ID列表
            LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
            courseWrapper.like(Course::getCourseName, keyword);
            List<Course> matchedCourses = courseMapper.selectList(courseWrapper);
            if (matchedCourses.isEmpty()) {
                // 没有匹配的课程，返回空结果
                Page<LearningProgressVO> emptyVoPage = new Page<>(current, size, 0);
                emptyVoPage.setRecords(new ArrayList<>());
                return emptyVoPage;
            }
            List<Long> matchedCourseIds = matchedCourses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
            // 由于UserPlan的courseId可能为空，需要通过TrainingPlan关联查询
            // 先查询匹配课程的培训计划
            LambdaQueryWrapper<TrainingPlan> planWrapper = new LambdaQueryWrapper<>();
            planWrapper.in(TrainingPlan::getCourseId, matchedCourseIds);
            List<TrainingPlan> matchedPlans = trainingPlanMapper.selectList(planWrapper);
            List<Long> matchedPlanIds = matchedPlans.stream()
                    .map(TrainingPlan::getId)
                    .collect(Collectors.toList());
            
            // 构建查询条件：课程ID匹配或培训计划ID匹配
            if (!matchedPlanIds.isEmpty()) {
                wrapper.and(w -> w.in(UserPlan::getCourseId, matchedCourseIds)
                                   .or().in(UserPlan::getPlanId, matchedPlanIds));
            } else {
                wrapper.in(UserPlan::getCourseId, matchedCourseIds);
            }
        }
        
        Page<UserPlan> userPlanPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<LearningProgressVO> voPage = new Page<>(current, size, userPlanPage.getTotal());
        
        if (userPlanPage.getRecords().isEmpty()) {
            voPage.setRecords(new ArrayList<>());
            return voPage;
        }
        
        // 批量获取用户信息
        Set<Long> userIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        Map<Long, String> deptNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            
            // 批量获取部门名称
            Set<Long> deptIds = users.stream()
                    .map(User::getDeptId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!deptIds.isEmpty()) {
                List<Dept> depts = deptMapper.selectBatchIds(deptIds);
                deptNameMap = depts.stream().collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
            }
        }
        
        // 批量获取培训计划信息
        Set<Long> planIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getPlanId)
                .collect(Collectors.toSet());
        Map<Long, TrainingPlan> planMap = new HashMap<>();
        if (!planIds.isEmpty()) {
            List<TrainingPlan> plans = trainingPlanMapper.selectBatchIds(planIds);
            planMap = plans.stream().collect(Collectors.toMap(TrainingPlan::getId, p -> p));
        }
        
        // 批量获取课程信息
        Set<Long> courseIds = userPlanPage.getRecords().stream()
                .map(UserPlan::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 从培训计划中获取关联的课程ID
        for (TrainingPlan plan : planMap.values()) {
            if (plan.getCourseId() != null) {
                courseIds.add(plan.getCourseId());
            }
        }
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseMap = courses.stream().collect(Collectors.toMap(Course::getId, c -> c));
        }
        
        // 转换为VO（已在数据库层面完成过滤，无需内存过滤）
        final Map<Long, User> finalUserMap = userMap;
        final Map<Long, String> finalDeptNameMap = deptNameMap;
        final Map<Long, TrainingPlan> finalPlanMap = planMap;
        final Map<Long, Course> finalCourseMap = courseMap;
        
        List<LearningProgressVO> voList = userPlanPage.getRecords().stream()
                .map(up -> {
                    LearningProgressVO vo = new LearningProgressVO();
                    BeanUtils.copyProperties(up, vo);
                    
                    // 设置用户信息
                    User u = finalUserMap.get(up.getUserId());
                    if (u != null) {
                        vo.setUserName(u.getUsername());
                        vo.setRealName(u.getRealName());
                        vo.setDeptName(finalDeptNameMap.get(u.getDeptId()));
                    }
                    
                    // 设置培训计划信息
                    TrainingPlan plan = finalPlanMap.get(up.getPlanId());
                    if (plan != null) {
                        vo.setPlanName(plan.getPlanName());
                    }
                    
                    // 设置课程信息
                    Long courseId = up.getCourseId() != null ? up.getCourseId() : 
                                   (plan != null ? plan.getCourseId() : null);
                    if (courseId != null) {
                        Course course = finalCourseMap.get(courseId);
                        if (course != null) {
                            vo.setCourseId(courseId);
                            vo.setCourseName(course.getCourseName());
                            vo.setCourseType(course.getCourseType());
                            vo.setCoverImage(course.getCoverImage());
                            vo.setCourseDesc(course.getCourseDesc());
                            vo.setDuration(course.getDuration());
                            vo.setCredit(course.getCredit());
                        }
                    }
                    
                    // 设置状态名称
                    vo.setStatusName(getStatusName(up.getStatus()));
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long planId, Long courseId, Integer progress) {
        // 参数校验
        if (planId == null || courseId == null || progress == null) {
            throw new BusinessException("参数不能为空");
        }
        // 进度值范围校验
        if (progress < 0 || progress > 100) {
            throw new BusinessException("进度值必须在0-100之间");
        }
        
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        String username = auth.getName();
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证培训计划存在且有效
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("培训计划不存在");
        }
        // 只允许已发布(1)或进行中(2)状态的计划进行学习
        if (plan.getStatus() != 1 && plan.getStatus() != 2) {
            throw new BusinessException("培训计划未发布或未开始，无法学习");
        }
        
        // 修复问题7：验证当前时间是否在培训计划有效期内
        LocalDate today = LocalDate.now();
        if (plan.getStartDate() != null && today.isBefore(plan.getStartDate())) {
            throw new BusinessException("培训计划尚未开始，开始日期：" + plan.getStartDate());
        }
        if (plan.getEndDate() != null && today.isAfter(plan.getEndDate())) {
            throw new BusinessException("培训计划已结束，无法更新进度。结束日期：" + plan.getEndDate());
        }
        
        // 验证用户是否有权限参与该培训计划（是否在目标范围内）
        if (!checkUserInPlanTarget(user, plan)) {
            throw new BusinessException("您不在该培训计划的目标范围内，无法学习");
        }
        
        // 查找或创建学习进度记录
        LambdaQueryWrapper<UserPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlan::getUserId, user.getId())
               .eq(UserPlan::getPlanId, planId);
        UserPlan userPlan = baseMapper.selectOne(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (userPlan == null) {
            userPlan = new UserPlan();
            userPlan.setUserId(user.getId());
            userPlan.setPlanId(planId);
            userPlan.setCourseId(courseId);
            userPlan.setProgress(progress);
            userPlan.setStatus(progress >= 100 ? 2 : 1); // 设置状态：0未开始、1进行中、2已完成
            userPlan.setLastStudyTime(now);
            if (progress >= 100) {
                userPlan.setCompleteTime(now);
            }
            baseMapper.insert(userPlan);
        } else {
            // 修复：当进度已达到100%时，不应再允许更新
            if (userPlan.getProgress() != null && userPlan.getProgress() >= 100) {
                throw new BusinessException("学习进度已完成，无需继续更新");
            }
            // 防止进度倒退
            if (userPlan.getProgress() != null && progress < userPlan.getProgress()) {
                throw new BusinessException("学习进度不能倒退");
            }
            userPlan.setProgress(progress);
            userPlan.setStatus(progress >= 100 ? 2 : 1);
            userPlan.setLastStudyTime(now); // 更新最后学习时间
            if (progress >= 100 && userPlan.getCompleteTime() == null) {
                userPlan.setCompleteTime(now);
            }
            baseMapper.updateById(userPlan);
        }
    }
    
    /**
     * 检查用户是否在培训计划的目标范围内
     * 修复：使用JSON解析进行精确ID匹配，避免contains()误匹配（如ID "1" 匹配到 "11", "21" 等）
     */
    private boolean checkUserInPlanTarget(User user, TrainingPlan plan) {
        // 获取培训计划的目标类型
        Integer targetType = plan.getTargetType();
        if (targetType == null) {
            // 如果没有设置目标类型，默认允许所有用户
            return true;
        }
        
        try {
            switch (targetType) {
                case 1: // 部门
                    String targetDeptIds = plan.getTargetDeptIds();
                    if (targetDeptIds == null || targetDeptIds.trim().isEmpty()) {
                        return true;
                    }
                    if (user.getDeptId() == null) {
                        return false;
                    }
                    // 使用JSON解析精确匹配部门ID
                    return isIdInJsonArray(targetDeptIds, user.getDeptId());
                    
                case 2: // 岗位
                    String targetPositionIds = plan.getTargetPositionIds();
                    if (targetPositionIds == null || targetPositionIds.trim().isEmpty()) {
                        return true;
                    }
                    if (user.getPositionId() == null) {
                        return false;
                    }
                    // 使用JSON解析精确匹配岗位ID
                    return isIdInJsonArray(targetPositionIds, user.getPositionId());
                    
                case 3: // 个人
                    String targetUserIds = plan.getTargetUserIds();
                    if (targetUserIds == null || targetUserIds.trim().isEmpty()) {
                        return true;
                    }
                    // 使用JSON解析精确匹配用户ID
                    return isIdInJsonArray(targetUserIds, user.getId());
                    
                default:
                    return true;
            }
        } catch (Exception e) {
            // JSON解析失败时，记录日志并返回false（安全原则：解析失败则不允许访问）
            return false;
        }
    }
    
    /**
     * 检查ID是否在JSON数组字符串中
     * 使用JSON解析器进行精确匹配，避免边界问题
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 ["1","2","3"]
     * @param targetId 要查找的ID
     * @return 是否包含该ID
     */
    private boolean isIdInJsonArray(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 使用JSON解析器进行精确匹配
            JSONArray jsonArray = JSON.parseArray(jsonArrayStr);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return false;
            }
            
            String targetIdStr = String.valueOf(targetId);
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                String itemIdStr = item != null ? item.toString().trim() : "";
                if (targetIdStr.equals(itemIdStr)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // JSON解析失败时，使用备用方法：正则表达式精确匹配
            log.warn("JSON解析失败，使用备用方法: {}", e.getMessage());
            // 使用正则表达式精确匹配数字ID
            String pattern = "(^|[,\\[\\]\\s])" + targetId + "([,\\[\\]\\s]|$)";
            return jsonArrayStr.matches(".*" + pattern + ".*");
        }
        return false;
    }
    
    @Override
    public LearningProgressVO getProgressDetail(Long id) {
        UserPlan userPlan = baseMapper.selectById(id);
        if (userPlan == null) {
            return null;
        }
        
        LearningProgressVO vo = new LearningProgressVO();
        BeanUtils.copyProperties(userPlan, vo);
        
        // 获取用户信息
        User user = userMapper.selectById(userPlan.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setRealName(user.getRealName());
            if (user.getDeptId() != null) {
                Dept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
        }
        
        // 获取培训计划信息
        TrainingPlan plan = trainingPlanMapper.selectById(userPlan.getPlanId());
        if (plan != null) {
            vo.setPlanName(plan.getPlanName());
        }
        
        // 修复：增加课程信息查询和填充
        Long courseId = userPlan.getCourseId() != null ? userPlan.getCourseId() : 
                       (plan != null ? plan.getCourseId() : null);
        if (courseId != null) {
            Course course = courseMapper.selectById(courseId);
            if (course != null) {
                vo.setCourseId(courseId);
                vo.setCourseName(course.getCourseName());
                vo.setCourseType(course.getCourseType());
                vo.setCoverImage(course.getCoverImage());
                vo.setCourseDesc(course.getCourseDesc());
                vo.setDuration(course.getDuration());
                vo.setCredit(course.getCredit());
            }
        }
        
        vo.setStatusName(getStatusName(userPlan.getStatus()));
        
        return vo;
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未开始";
            case 1: return "进行中";
            case 2: return "已完成";
            default: return "未知";
        }
    }
}
