package com.etms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试记录视图对象
 */
@Data
public class ExamRecordVO {
    
    private Long id;
    
    private Long paperId;
    
    private String paperName;
    
    private Long userId;
    
    private String userName;
    
    private String realName;
    
    private String deptName;
    
    private Long planId;
    
    private String planName;
    
    private Integer status;
    
    private Integer totalScore;
    
    private Integer userScore;
    
    private Integer passScore;
    
    /** 考试时长(分钟) - 试卷规定的考试时间限制 */
    private Integer duration;
    
    private Integer passed;

    /** 实际用时(分钟) */
    private Integer durationUsed;

    /** 正确题数 */
    private Integer correctCount;
    
    /** 总题数 */
    private Integer totalCount;
    
    private LocalDateTime startTime;
    
    private LocalDateTime submitTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}
