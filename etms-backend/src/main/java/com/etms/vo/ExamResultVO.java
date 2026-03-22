package com.etms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 成绩视图对象
 */
@Data
public class ExamResultVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 记录ID */
    private Long id;
    
    /** 试卷ID */
    private Long paperId;
    
    /** 试卷名称 */
    private String paperName;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String userName;
    
    /** 真实姓名 */
    private String realName;
    
    /** 部门名称 */
    private String deptName;
    
    /** 总分 */
    private Integer totalScore;
    
    /** 得分 */
    private Integer userScore;
    
    /** 及格分 */
    private Integer passScore;
    
    /** 是否通过：0-否 1-是 */
    private Integer passed;
    
    /** 考试时长（分钟） */
    private Integer examDuration;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 提交时间 */
    private LocalDateTime submitTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
