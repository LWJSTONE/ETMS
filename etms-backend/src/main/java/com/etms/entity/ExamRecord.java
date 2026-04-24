package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考试记录实体
 * 修复：不再继承BaseEntity，因为exam_record表没有deleted列
 * 继承BaseEntity会导致MyBatis-Plus全局逻辑删除配置(@TableLogic)对该表生效，
 * 自动在SQL中追加"AND deleted = 0"条件，但表没有deleted列，导致SQL错误：
 * "Unknown column 'deleted' in 'where clause'"，这是"我的考试不能进行"的根本原因
 */
@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 考试状态：0-考试中 1-已提交 2-超时 3-已批阅 4-已放弃
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
