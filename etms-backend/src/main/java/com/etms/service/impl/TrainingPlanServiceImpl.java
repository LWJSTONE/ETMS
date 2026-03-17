package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.TrainingPlan;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 培训计划服务实现类
 */
@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {
    
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
    public boolean addPlan(TrainingPlan plan) {
        plan.setStatus(0); // 草稿状态
        return baseMapper.insert(plan) > 0;
    }
    
    @Override
    public boolean updatePlan(TrainingPlan plan) {
        return baseMapper.updateById(plan) > 0;
    }
    
    @Override
    public boolean deletePlan(Long id) {
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean publishPlan(Long id) {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(1); // 已发布
        return baseMapper.updateById(plan) > 0;
    }
    
    @Override
    public boolean archivePlan(Long id) {
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
