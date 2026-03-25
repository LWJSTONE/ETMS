package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 考试记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends BaseEntity {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 试卷ID
     */
    private Long paperId;
    
    /**
     * 培训计划ID
     */
    private Long planId;
    
    /**
     * 考试状态：1-进行中 2-已完成 3-已超时 4-已放弃
     */
    private Integer status;
    
    /**
     * 总分
     */
    private Integer totalScore;
    
    /**
     * 得分
     */
    private Integer userScore;
    
    /**
     * 是否通过：0-否 1-是
     */
    private Integer passed;
    
    /**
     * 考试开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("exam_start_time")
    private LocalDateTime startTime;
    
    /**
     * 考试结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("exam_end_time")
    private LocalDateTime endTime;
    
    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 实际用时（分钟）
     */
    private Integer durationUsed;

    /**
     * 切屏次数（防作弊）
     */
    private Integer switchCount;

    /**
     * 客观题得分
     */
    private Integer objectiveScore;

    /**
     * 主观题得分
     */
    private Integer subjectiveScore;

    /**
     * 答题详情（JSON格式）
     */
    private String answerDetail;

    /**
     * 补考次数
     */
    private Integer retakeCount;
}
