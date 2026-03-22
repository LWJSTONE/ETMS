package com.etms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考试记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("etms_exam_record")
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
     * 考试状态：0-未开始 1-进行中 2-已完成 3-已超时
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
     * 开始时间
     */
    private java.time.LocalDateTime startTime;
    
    /**
     * 提交时间
     */
    private java.time.LocalDateTime submitTime;
}
