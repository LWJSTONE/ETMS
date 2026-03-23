package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.entity.UserPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.vo.LearningProgressVO;

/**
 * 学习进度服务接口
 */
public interface LearningProgressService extends IService<UserPlan> {
    
    /**
     * 分页查询学习进度
     */
    Page<LearningProgressVO> pageProgress(Long current, Long size, Long planId, Long userId, Integer status, String userName, String planName);
    
    /**
     * 获取我的学习进度
     */
    Page<LearningProgressVO> getMyProgress(Long current, Long size, Integer status, String keyword, Long planId);
    
    /**
     * 更新学习进度
     */
    void updateProgress(Long planId, Long courseId, Integer progress);
    
    /**
     * 获取学习进度详情
     */
    LearningProgressVO getProgressDetail(Long id);
}
