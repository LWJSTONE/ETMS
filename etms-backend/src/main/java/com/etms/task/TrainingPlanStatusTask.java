package com.etms.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.etms.entity.TrainingPlan;
import com.etms.mapper.TrainingPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 培训计划状态自动更新定时任务
 * 修复问题：培训计划缺少自动状态更新机制
 * 
 * 功能说明：
 * 1. 将已发布(1)且到达开始日期的计划自动更新为进行中(2)
 * 2. 将进行中(2)且已过结束日期的计划自动更新为已结束(3)
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TrainingPlanStatusTask {
    
    private final TrainingPlanMapper trainingPlanMapper;
    
    /**
     * 自动更新培训计划状态
     * 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoUpdatePlanStatus() {
        log.info("开始执行培训计划状态自动更新任务...");
        
        LocalDate today = LocalDate.now();
        int updatedCount = 0;
        
        try {
            // 1. 将已发布(1)且到达开始日期的计划更新为进行中(2)
            LambdaUpdateWrapper<TrainingPlan> startWrapper = new LambdaUpdateWrapper<>();
            startWrapper.eq(TrainingPlan::getStatus, 1) // 已发布
                       .le(TrainingPlan::getStartDate, today) // 开始日期<=今天
                       .ge(TrainingPlan::getEndDate, today)   // 结束日期>=今天（还未结束）
                       .set(TrainingPlan::getStatus, 2);      // 更新为进行中
            
            int startedCount = trainingPlanMapper.update(null, startWrapper);
            if (startedCount > 0) {
                log.info("已将 {} 个培训计划状态更新为进行中", startedCount);
                updatedCount += startedCount;
            }
            
            // 2. 将进行中(2)且已过结束日期的计划更新为已结束(3)
            LambdaUpdateWrapper<TrainingPlan> endWrapper = new LambdaUpdateWrapper<>();
            endWrapper.eq(TrainingPlan::getStatus, 2) // 进行中
                      .lt(TrainingPlan::getEndDate, today) // 结束日期<今天（已过期）
                      .set(TrainingPlan::getStatus, 3);    // 更新为已结束
            
            int endedCount = trainingPlanMapper.update(null, endWrapper);
            if (endedCount > 0) {
                log.info("已将 {} 个培训计划状态更新为已结束", endedCount);
                updatedCount += endedCount;
            }
            
            // 3. 将已发布(1)但已过结束日期的计划直接更新为已结束(3)
            // 处理那些发布后未开始就已过期的计划
            LambdaUpdateWrapper<TrainingPlan> expiredWrapper = new LambdaUpdateWrapper<>();
            expiredWrapper.eq(TrainingPlan::getStatus, 1) // 已发布
                         .lt(TrainingPlan::getEndDate, today) // 结束日期<今天（已过期）
                         .set(TrainingPlan::getStatus, 3);    // 更新为已结束
            
            int expiredCount = trainingPlanMapper.update(null, expiredWrapper);
            if (expiredCount > 0) {
                log.info("已将 {} 个过期的培训计划状态更新为已结束", expiredCount);
                updatedCount += expiredCount;
            }
            
            log.info("培训计划状态自动更新任务完成，共更新 {} 条记录", updatedCount);
            
        } catch (Exception e) {
            log.error("培训计划状态自动更新任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查并更新单个培训计划状态（供手动调用）
     * @param planId 培训计划ID
     */
    public void checkAndUpdatePlanStatus(Long planId) {
        if (planId == null) {
            return;
        }
        
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        Integer currentStatus = plan.getStatus();
        Integer newStatus = null;
        
        // 根据日期判断应该的状态
        if (currentStatus == 1) { // 已发布
            if (plan.getEndDate() != null && plan.getEndDate().isBefore(today)) {
                newStatus = 3; // 已结束
            } else if (plan.getStartDate() != null && !plan.getStartDate().isAfter(today)) {
                newStatus = 2; // 进行中
            }
        } else if (currentStatus == 2) { // 进行中
            if (plan.getEndDate() != null && plan.getEndDate().isBefore(today)) {
                newStatus = 3; // 已结束
            }
        }
        
        // 更新状态
        if (newStatus != null && !newStatus.equals(currentStatus)) {
            LambdaUpdateWrapper<TrainingPlan> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TrainingPlan::getId, planId)
                        .set(TrainingPlan::getStatus, newStatus);
            trainingPlanMapper.update(null, updateWrapper);
            log.info("培训计划[{}]状态从{}更新为{}", planId, currentStatus, newStatus);
        }
    }
}
