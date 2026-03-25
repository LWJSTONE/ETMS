package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 试卷实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_paper")
public class Paper extends BaseEntity {
    
    /** 试卷名称 */
    private String paperName;
    
    /** 试卷编码 */
    private String paperCode;
    
    /** 关联培训计划ID */
    private Long planId;
    
    /** 关联课程ID */
    private Long courseId;
    
    /** 试卷类型(1手动组卷 2随机组卷) */
    private Integer paperType;
    
    /** 总分 */
    private Integer totalScore;
    
    /** 及格分数 */
    private Integer passScore;
    
    /** 考试时长(分钟) - 前端映射为duration */
    @JsonProperty("duration")
    private Integer examDuration;
    
    /** 考试开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /** 考试结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /** 题目数量 */
    private Integer questionCount;
    
    /** 题目配置(JSON) */
    private String questionConfig;
    
    /** 是否打乱选项 */
    private Integer shuffleOption;
    
    /** 是否打乱题目 */
    private Integer shuffleQuestion;
    
    /** 防作弊开关 */
    private Integer antiCheat;
    
    /** 最大切屏次数 */
    private Integer maxSwitch;
    
    /** 状态(0草稿 1已发布 2已停用) */
    private Integer status;
    
    /** 试卷描述 */
    private String description;
    
    @TableField(exist = false)
    private String planName;
    
    @TableField(exist = false)
    private String courseName;
}
