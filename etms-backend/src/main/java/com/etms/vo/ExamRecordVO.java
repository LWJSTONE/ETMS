package com.etms.vo;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

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
    
    /** 修复：添加日期格式注解，确保前端能正确解析日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /** 修复：添加日期格式注解 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    /** 答题详情(JSON格式) */
    private String answerDetail;
    
    /** 试卷题目列表（考试进行中时返回，用于考试答题页面展示题目） */
    private List<PaperQuestionVO> questions;
    
    /** 修复：添加日期格式注解 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
