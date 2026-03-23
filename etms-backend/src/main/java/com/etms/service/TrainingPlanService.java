package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.TrainingPlan;
import com.etms.vo.TrainingPlanVO;

/**
 * 培训计划服务接口
 */
public interface TrainingPlanService extends IService<TrainingPlan> {
    
    /**
     * 分页查询培训计划列表
     */
    Page<TrainingPlanVO> pagePlans(Page<TrainingPlan> page, String planName, Integer status, Integer planType, String startDate, String endDate, Long deptId);
    
    /**
     * 获取培训计划详情
     */
    TrainingPlanVO getPlanDetail(Long id);
    
    /**
     * 新增培训计划
     */
    boolean addPlan(TrainingPlan plan);
    
    /**
     * 更新培训计划
     */
    boolean updatePlan(TrainingPlan plan);
    
    /**
     * 删除培训计划
     */
    boolean deletePlan(Long id);
    
    /**
     * 发布培训计划
     */
    boolean publishPlan(Long id);
    
    /**
     * 归档培训计划
     */
    boolean archivePlan(Long id);
    
    /**
     * 结束培训计划
     */
    boolean endPlan(Long id);
}
