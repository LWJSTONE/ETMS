package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.TrainingPlan;
import com.etms.entity.UserPlan;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.UserPlanMapper;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 培训计划服务实现类
 */
@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {
    
    private final UserPlanMapper userPlanMapper;
    
    @Override
    public Page<TrainingPlanVO> pagePlans(Page<TrainingPlan> page, String planName, Integer status, Integer planType) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planName), TrainingPlan::getPlanName, planName)
               .eq(status != null, TrainingPlan::getStatus, status)
               .eq(planType != null, TrainingPlan::getPlanType, planType)
               .orderByDesc(TrainingPlan::getCreateTime);
        
        Page<TrainingPlan> planPage = baseMapper.selectPage(page, wrapper);
        
        Page<TrainingPlanVO> voPage = new Page<>();
        BeanUtils.copyProperties(planPage, voPage, "records");
        
        List<TrainingPlanVO> voList = planPage.getRecords().stream().map(plan -> {
            TrainingPlanVO vo = new TrainingPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setPlanTypeName(getPlanTypeName(plan.getPlanType()));
            vo.setStatusName(getStatusName(plan.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public TrainingPlanVO getPlanDetail(Long id) {
        TrainingPlan plan = baseMapper.selectById(id);
        if (plan == null) {
            return null;
        }
        
        TrainingPlanVO vo = new TrainingPlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setPlanTypeName(getPlanTypeName(plan.getPlanType()));
        vo.setStatusName(getStatusName(plan.getStatus()));
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPlan(TrainingPlan plan) {
        plan.setStatus(0); // 草稿状态
        return baseMapper.insert(plan) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlan(TrainingPlan plan) {
        return baseMapper.updateById(plan) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlan(Long id) {
        // 检查培训计划是否有学习记录
        Long count = userPlanMapper.selectCount(
            new LambdaQueryWrapper<UserPlan>().eq(UserPlan::getPlanId, id)
        );
        if (count > 0) {
            throw new RuntimeException("培训计划存在学习记录，无法删除");
        }
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new RuntimeException("培训计划不存在");
        }
        
        // 校验状态流转：只能从草稿(0)状态发布
        if (existingPlan.getStatus() != 0) {
            throw new RuntimeException("当前状态不允许发布，只有草稿状态可以发布");
        }
        
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(1); // 已发布
        return baseMapper.updateById(plan) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archivePlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new RuntimeException("培训计划不存在");
        }
        
        // 校验状态流转：只能从已结束(3)状态归档
        if (existingPlan.getStatus() != 3) {
            throw new RuntimeException("当前状态不允许归档，只有已结束状态可以归档");
        }
        
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(4); // 已归档
        return baseMapper.updateById(plan) > 0;
    }
    
    private String getPlanTypeName(Integer planType) {
        if (planType == null) return "未知";
        return planType == 1 ? "必修" : "选修";
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "进行中";
            case 3: return "已结束";
            case 4: return "已归档";
            default: return "未知";
        }
    }
}
