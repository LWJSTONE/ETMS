package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_course")
public class Course extends BaseEntity {
    
    /** 课程名称 */
    private String courseName;
    
    /** 课程编码 */
    private String courseCode;
    
    /** 课程描述 */
    private String courseDesc;
    
    /** 课程目标 */
    private String courseObjective;
    
    /** 课程类型(1视频 2文档 3直播) */
    private Integer courseType;
    
    /** 课程时长(分钟) */
    private Integer duration;
    
    /** 学分 */
    private Integer credit;
    
    /** 难度等级(1入门 2初级 3中级 4高级 5专家) */
    private Integer difficulty;
    
    /** 封面图片URL */
    private String coverImage;
    
    /** 视频资源URL */
    private String videoUrl;
    
    /** 文档资源URL */
    private String documentUrl;
    
    /** PPT资源URL */
    private String pptUrl;
    
    /** 标签ID集合(JSON) */
    private String tagIds;
    
    /** 适用对象 */
    private String targetAudience;
    
    /** 前置要求 */
    private String prerequisite;
    
    /** 状态(0草稿 1待审核 2已上架 3已下架 4审核驳回) */
    private Integer status;
    
    /** 审核意见 */
    private String auditRemark;
    
    /** 审核人ID */
    private Long auditBy;
    
    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    /** 浏览次数 */
    private Integer viewCount;
    
    /** 收藏次数 */
    private Integer collectCount;
    
    /** 平均评分 */
    private BigDecimal ratingAvg;
    
    /** 评分人数 */
    private Integer ratingCount;
    
}
