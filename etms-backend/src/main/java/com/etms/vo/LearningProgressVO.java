package com.etms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学习进度VO
 */
@Data
public class LearningProgressVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 记录ID */
    private Long id;
    
    /** 培训计划ID */
    private Long planId;
    
    /** 培训计划名称 */
    private String planName;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String userName;
    
    /** 真实姓名 */
    private String realName;
    
    /** 部门名称 */
    private String deptName;
    
    /** 课程ID */
    private Long courseId;
    
    /** 课程名称 */
    private String courseName;
    
    /** 课程类型(1视频 2文档 3直播) */
    private Integer courseType;
    
    /** 封面图片 */
    private String coverImage;
    
    /** 课程描述 */
    private String courseDesc;
    
    /** 课程时长(分钟) */
    private Integer duration;
    
    /** 学分 */
    private Integer credit;
    
    /** 学习进度（百分比） */
    private Integer progress;
    
    /** 学习时长(分钟) */
    private Integer studyTime;
    
    /** 最后学习时间 */
    private LocalDateTime lastStudyTime;
    
    /** 学习状态：0-未开始 1-进行中 2-已完成 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 完成时间 */
    private LocalDateTime completeTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
}
