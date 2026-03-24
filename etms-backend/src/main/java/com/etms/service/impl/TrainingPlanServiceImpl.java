package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.TrainingPlan;
import com.etms.entity.UserPlan;
import com.etms.entity.Course;
import com.etms.entity.PlanCourse;
import com.etms.exception.BusinessException;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.UserPlanMapper;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.PlanCourseMapper;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 培训计划服务实现类
 */
@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {
    
    private final UserPlanMapper userPlanMapper;
    private final CourseMapper courseMapper;
    private final PlanCourseMapper planCourseMapper;
    
    @Override
    public Page<TrainingPlanVO> pagePlans(Page<TrainingPlan> page, String planName, Integer status, Integer planType, String startDate, String endDate, Long deptId) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planName), TrainingPlan::getPlanName, planName)
               .eq(status != null, TrainingPlan::getStatus, status)
               .eq(planType != null, TrainingPlan::getPlanType, planType)
               // 日期筛选：筛选开始日期在指定范围内
               .ge(StringUtils.hasText(startDate), TrainingPlan::getStartDate, LocalDate.parse(startDate))
               .le(StringUtils.hasText(endDate), TrainingPlan::getEndDate, LocalDate.parse(endDate))
               .orderByDesc(TrainingPlan::getCreateTime);
        
        // 修复：部门筛选使用精确的JSON解析匹配，避免LIKE匹配导致的误匹配（如ID "1" 匹配到 "11", "21" 等）
        Page<TrainingPlan> planPage;
        if (deptId != null) {
            // 查询所有符合其他条件的数据
            List<TrainingPlan> allPlans = baseMapper.selectList(wrapper);
            // 使用精确的JSON解析匹配进行过滤
            List<TrainingPlan> filteredPlans = allPlans.stream()
                    .filter(plan -> isIdInJsonArray(plan.getTargetDeptIds(), deptId))
                    .collect(Collectors.toList());
            
            // 手动分页
            long total = filteredPlans.size();
            int current = (int) page.getCurrent();
            int size = (int) page.getSize();
            int fromIndex = (current - 1) * size;
            int toIndex = Math.min(fromIndex + size, filteredPlans.size());
            
            List<TrainingPlan> pagedPlans = fromIndex < filteredPlans.size() 
                    ? filteredPlans.subList(fromIndex, toIndex) 
                    : new ArrayList<>();
            
            planPage = new Page<>(current, size, total);
            planPage.setRecords(pagedPlans);
        } else {
            planPage = baseMapper.selectPage(page, wrapper);
        }
        
        Page<TrainingPlanVO> voPage = new Page<>();
        BeanUtils.copyProperties(planPage, voPage, "records");
        
        List<TrainingPlanVO> voList = planPage.getRecords().stream().map(plan -> {
            TrainingPlanVO vo = new TrainingPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setPlanTypeName(getPlanTypeName(plan.getPlanType()));
            vo.setStatusName(getStatusName(plan.getStatus()));
            // 设置课程名称
            if (plan.getCourseId() != null) {
                Course course = courseMapper.selectById(plan.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
            }
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
        
        // 设置课程名称
        if (plan.getCourseId() != null) {
            Course course = courseMapper.selectById(plan.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }
        
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
        // 获取培训计划
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 只有草稿状态才能删除
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的培训计划才能删除");
        }
        
        // 检查培训计划是否有学习记录
        Long count = userPlanMapper.selectCount(
            new LambdaQueryWrapper<UserPlan>().eq(UserPlan::getPlanId, id)
        );
        if (count > 0) {
            throw new BusinessException("培训计划存在学习记录，无法删除");
        }
        
        // 删除培训计划时清理PlanCourse关联数据
        planCourseMapper.delete(
            new LambdaQueryWrapper<PlanCourse>().eq(PlanCourse::getPlanId, id)
        );
        
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
        
        // 验证开始日期不能早于当前日期
        if (existingPlan.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException("开始日期不能早于当前日期");
        }
        
        // 验证必要字段：培训类型
        if (existingPlan.getPlanType() == null) {
            throw new BusinessException("请先设置培训类型");
        }
        
        // 验证必要字段：目标类型
        if (existingPlan.getTargetType() == null) {
            throw new BusinessException("请先设置目标类型");
        }
        
        // 根据目标类型验证不同的目标字段
        Integer targetType = existingPlan.getTargetType();
        if (targetType == 1) {
            // 按部门：验证目标部门
            if (existingPlan.getTargetDeptIds() == null || existingPlan.getTargetDeptIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标部门");
            }
            // 验证JSON数组不为空
            if (isJsonArrayEmpty(existingPlan.getTargetDeptIds())) {
                throw new BusinessException("请至少选择一个目标部门");
            }
        } else if (targetType == 2) {
            // 按岗位：验证目标岗位
            if (existingPlan.getTargetPositionIds() == null || existingPlan.getTargetPositionIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标岗位");
            }
            if (isJsonArrayEmpty(existingPlan.getTargetPositionIds())) {
                throw new BusinessException("请至少选择一个目标岗位");
            }
        } else if (targetType == 3) {
            // 按人员：验证目标人员
            if (existingPlan.getTargetUserIds() == null || existingPlan.getTargetUserIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标人员");
            }
            if (isJsonArrayEmpty(existingPlan.getTargetUserIds())) {
                throw new BusinessException("请至少选择一个目标人员");
            }
        }
        
        // 验证必要字段：关联课程
        if (existingPlan.getCourseId() == null) {
            throw new BusinessException("请先关联培训课程");
        }
        
        // 验证关联课程是否存在且已上架
        Course course = courseMapper.selectById(existingPlan.getCourseId());
        if (course == null) {
            throw new BusinessException("关联的课程不存在");
        }
        if (course.getStatus() != 2) {
            throw new BusinessException("关联的课程未上架，无法发布培训计划");
        }
        
        // 修复：使用乐观锁防止并发重复发布
        int updateCount = baseMapper.updateStatusWithOptimisticLock(id, 0, 1);
        if (updateCount == 0) {
            throw new BusinessException("发布失败，培训计划状态已被修改，请刷新后重试");
        }
        return true;
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
        
        // 修复：使用乐观锁防止并发重复归档
        int updateCount = baseMapper.updateStatusWithOptimisticLock(id, 3, 4);
        if (updateCount == 0) {
            throw new BusinessException("归档失败，培训计划状态已被修改，请刷新后重试");
        }
        return true;
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
        
        // 修复：使用乐观锁防止并发重复结束
        // 已发布(1)或进行中(2)状态可以结束
        int updateCount = 0;
        if (existingPlan.getStatus() == 1) {
            updateCount = baseMapper.updateStatusWithOptimisticLock(id, 1, 3);
        } else if (existingPlan.getStatus() == 2) {
            updateCount = baseMapper.updateStatusWithOptimisticLock(id, 2, 3);
        }
        if (updateCount == 0) {
            throw new BusinessException("结束失败，培训计划状态已被修改，请刷新后重试");
        }
        return true;
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
    
    /**
     * 检查ID是否在JSON数组字符串中
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 ["1","2","3"]
     * @param targetId 要查找的ID
     * @return 是否包含该ID
     */
    private boolean isIdInJsonArray(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return false;
        }
        
        String str = jsonArrayStr.trim();
        // 处理JSON数组格式
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }
        
        // 分割并精确匹配
        String[] parts = str.split(",");
        for (String part : parts) {
            String idStr = part.trim().replace("\"", "");
            if (idStr.equals(String.valueOf(targetId))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查JSON数组字符串是否为空
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 "[]"
     * @return 是否为空数组
     */
    private boolean isJsonArrayEmpty(String jsonArrayStr) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return true;
        }
        
        String str = jsonArrayStr.trim();
        // 处理JSON数组格式
        if (str.equals("[]")) {
            return true;
        }
        
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1).trim();
            // 移除所有空白后检查是否为空
            if (str.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
