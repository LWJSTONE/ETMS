package com.etms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户培训计划学习记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("etms_user_plan")
public class UserPlan extends BaseEntity {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 培训计划ID
     */
    private Long planId;
    
    /**
     * 学习进度（百分比）
     */
    private Integer progress;
    
    /**
     * 学习状态：0-未开始 1-进行中 2-已完成
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private java.time.LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    private java.time.LocalDateTime completeTime;
}
