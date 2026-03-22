package com.etms.service.impl;

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
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习进度服务实现类
 */
@Service
@RequiredArgsConstructor
public class LearningProgressServiceImpl extends ServiceImpl<UserPlanMapper, UserPlan> implements LearningProgressService {
    
    private final UserMapper userMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    private final CourseMapper courseMapper;
    private final DeptMapper deptMapper;
    
    @Override
    public Page<LearningProgressVO> pageProgress(Long current, Long size, Long planId, Long userId, Integer status, String userName, String planName) {
        Page<UserPlan> page = new Page<>(current, size);
        
        LambdaQueryWrapper<UserPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, UserPlan::getPlanId, planId)
               .eq(userId != null, UserPlan::getUserId, userId)
               .eq(status != null, UserPlan::getStatus, status)
               .orderByDesc(UserPlan::getCreateTime);
        
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
        
        // 过滤不符合条件的记录
        final Map<Long, User> finalUserMap = userMap;
        final Map<Long, String> finalDeptNameMap = deptNameMap;
        final Map<Long, TrainingPlan> finalPlanMap = planMap;
        
        List<LearningProgressVO> voList = userPlanPage.getRecords().stream()
                .filter(up -> {
                    // 过滤用户名和计划名
                    if (userName != null && !userName.isEmpty()) {
                        User user = finalUserMap.get(up.getUserId());
                        if (user == null || 
                            (user.getRealName() == null || !user.getRealName().contains(userName)) &&
                            (user.getUsername() == null || !user.getUsername().contains(userName))) {
                            return false;
                        }
                    }
                    if (planName != null && !planName.isEmpty()) {
                        TrainingPlan plan = finalPlanMap.get(up.getPlanId());
                        if (plan == null || plan.getPlanName() == null || !plan.getPlanName().contains(planName)) {
                            return false;
                        }
                    }
                    return true;
                })
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
                    
                    // 设置状态名称
                    vo.setStatusName(getStatusName(up.getStatus()));
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public Page<LearningProgressVO> getMyProgress(Long current, Long size, Integer status) {
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
        
        return pageProgress(current, size, null, user.getId(), status, null, null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long planId, Long courseId, Integer progress) {
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
        
        // 查找或创建学习进度记录
        LambdaQueryWrapper<UserPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlan::getUserId, user.getId())
               .eq(UserPlan::getPlanId, planId);
        UserPlan userPlan = baseMapper.selectOne(wrapper);
        
        if (userPlan == null) {
            userPlan = new UserPlan();
            userPlan.setUserId(user.getId());
            userPlan.setPlanId(planId);
            userPlan.setProgress(progress);
            userPlan.setStatus(progress >= 100 ? 2 : 1);
            userPlan.setStartTime(LocalDateTime.now());
            if (progress >= 100) {
                userPlan.setCompleteTime(LocalDateTime.now());
            }
            baseMapper.insert(userPlan);
        } else {
            userPlan.setProgress(progress);
            userPlan.setStatus(progress >= 100 ? 2 : 1);
            if (progress >= 100 && userPlan.getCompleteTime() == null) {
                userPlan.setCompleteTime(LocalDateTime.now());
            }
            baseMapper.updateById(userPlan);
        }
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
