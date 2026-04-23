package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 培训计划响应VO
 */
@Data
public class TrainingPlanVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 计划ID */
    private Long id;
    
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
    
    /** 课程名称 */
    private String courseName;
    
    /** 计划类型 */
    private Integer planType;
    
    /** 计划类型名称 */
    private String planTypeName;
    
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
    
    /** 目标类型 */
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
    
    /** 状态 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
