package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 培训计划实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_plan")
public class TrainingPlan extends BaseEntity {
    
    /** 计划名称 */
    private String planName;
    
    /** 计划编码 */
    private String planCode;
    
    /** 计划描述 */
    private String planDesc;
    
    /** 培训目标 */
    private String planObjective;
    
    /** 课程ID */
    private Long courseId;
    
    /** 计划类型(1必修 2选修) */
    private Integer planType;
    
    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /** 签到开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signStartTime;
    
    /** 签到结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signEndTime;
    
    /** 目标类型(1部门 2岗位 3个人) */
    private Integer targetType;
    
    /** 目标部门ID集合(JSON) */
    private String targetDeptIds;
    
    /** 目标岗位ID集合(JSON) */
    private String targetPositionIds;
    
    /** 目标用户ID集合(JSON) */
    private String targetUserIds;
    
    /** 最低学习时长(分钟) */
    private Integer minStudyTime;
    
    /** 最低完成进度(%) */
    private Integer minProgress;
    
    /** 是否需要考试 */
    private Integer needExam;
    
    /** 及格分数 */
    private Integer passScore;
    
    /** 最大补考次数 */
    private Integer maxRetake;

    /** 关联试卷ID */
    private Long paperId;

    /** 状态(0草稿 1已发布 2进行中 3已结束 4已归档) */
    private Integer status;
    
    @TableField(exist = false)
    private String courseName;
}
