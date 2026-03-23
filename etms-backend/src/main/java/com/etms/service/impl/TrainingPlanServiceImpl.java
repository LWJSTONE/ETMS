package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.TrainingPlan;
import com.etms.entity.UserPlan;
import com.etms.exception.BusinessException;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.UserPlanMapper;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Page<TrainingPlanVO> pagePlans(Page<TrainingPlan> page, String planName, Integer status, Integer planType, String startDate, String endDate, Long deptId) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planName), TrainingPlan::getPlanName, planName)
               .eq(status != null, TrainingPlan::getStatus, status)
               .eq(planType != null, TrainingPlan::getPlanType, planType)
               // 日期筛选：筛选开始日期在指定范围内
               .ge(StringUtils.hasText(startDate), TrainingPlan::getStartDate, LocalDate.parse(startDate))
               .le(StringUtils.hasText(endDate), TrainingPlan::getEndDate, LocalDate.parse(endDate))
               // 部门筛选：targetDeptIds是JSON数组，使用LIKE匹配
               .apply(deptId != null, "target_dept_ids LIKE CONCAT('%', {0}, '%')", deptId.toString())
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
        // 验证日期合理性
        if (plan.getStartDate() != null && plan.getEndDate() != null) {
            if (plan.getStartDate().isAfter(plan.getEndDate())) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
        }
        
        plan.setStatus(0); // 草稿状态
        return baseMapper.insert(plan) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlan(TrainingPlan plan) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(plan.getId());
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只允许草稿状态(0)编辑
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("当前状态不允许编辑，只有草稿状态可以编辑");
        }
        
        // 验证日期合理性
        LocalDate startDate = plan.getStartDate() != null ? plan.getStartDate() : existingPlan.getStartDate();
        LocalDate endDate = plan.getEndDate() != null ? plan.getEndDate() : existingPlan.getEndDate();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        
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
            throw new BusinessException("培训计划存在学习记录，无法删除");
        }
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从草稿(0)状态发布
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("当前状态不允许发布，只有草稿状态可以发布");
        }
        
        // 验证必要字段：培训名称
        if (existingPlan.getPlanName() == null || existingPlan.getPlanName().trim().isEmpty()) {
            throw new BusinessException("请先设置培训计划名称");
        }
        
        // 验证必要字段：日期
        if (existingPlan.getStartDate() == null || existingPlan.getEndDate() == null) {
            throw new BusinessException("请先设置培训计划的开始日期和结束日期");
        }
        
        // 验证日期合理性
        if (existingPlan.getStartDate().isAfter(existingPlan.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        
        // 验证必要字段：培训类型
        if (existingPlan.getPlanType() == null) {
            throw new BusinessException("请先设置培训类型");
        }
        
        // 验证必要字段：目标部门（至少选择一个部门）
        if (existingPlan.getTargetDeptIds() == null || existingPlan.getTargetDeptIds().trim().isEmpty()) {
            throw new BusinessException("请先设置目标部门");
        }
        
        // 验证必要字段：关联课程
        if (existingPlan.getCourseId() == null) {
            throw new BusinessException("请先关联培训课程");
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
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从已结束(3)状态归档
        if (existingPlan.getStatus() != 3) {
            throw new BusinessException("当前状态不允许归档，只有已结束状态可以归档");
        }
        
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(4); // 已归档
        return baseMapper.updateById(plan) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endPlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从已发布(1)或进行中(2)状态结束
        if (existingPlan.getStatus() != 1 && existingPlan.getStatus() != 2) {
            throw new BusinessException("当前状态不允许结束，只有已发布或进行中状态可以结束");
        }
        
        TrainingPlan plan = new TrainingPlan();
        plan.setId(id);
        plan.setStatus(3); // 已结束
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
