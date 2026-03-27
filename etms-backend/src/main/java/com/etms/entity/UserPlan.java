package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户培训计划学习记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("learning_progress")
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
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 学习进度（百分比）
     */
    private Integer progress;
    
    /**
     * 学习时长（分钟）
     */
    private Integer studyTime;
    
    /**
     * 学习状态：0-未开始 1-进行中 2-已完成 3-已超时
     */
    private Integer status;
    
    /**
     * 最后学习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastStudyTime;
    
    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    
    /**
     * 是否进度滞后
     */
    private Integer isLate;
    
    /**
     * 预警通知是否发送
     */
    private Integer warningSent;
    
    /**
     * 重写deleted字段，因为learning_progress表没有deleted字段
     * 标记为不存在于数据库表中，避免MyBatis-Plus自动添加deleted条件
     */
    @TableField(exist = false)
    private Integer deleted;
}
