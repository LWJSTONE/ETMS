package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户培训计划学习记录实体
 * 修复：不再继承BaseEntity，因为learning_progress表没有deleted、create_by、update_by、remark列
 * 继承BaseEntity会导致MyBatis-Plus全局逻辑删除配置(@TableLogic)对该表生效，
 * 自动在SQL中追加"AND deleted = 0"条件，但表没有deleted列，导致SQL错误：
 * "Unknown column 'deleted' in 'where clause'"
 */
@Data
@TableName("learning_progress")
public class UserPlan implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
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

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
