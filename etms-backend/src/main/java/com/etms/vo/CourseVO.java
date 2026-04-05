package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程响应VO
 */
@Data
public class CourseVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 课程ID */
    private Long id;
    
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
    
    /** 课程类型名称 */
    private String courseTypeName;
    
    /** 课程时长(分钟) */
    private Integer duration;
    
    /** 学分 */
    private Integer credit;
    
    /** 难度等级 */
    private Integer difficulty;
    
    /** 难度名称 */
    private String difficultyName;
    
    /** 封面图片URL */
    private String coverImage;
    
    /** 视频资源URL */
    private String videoUrl;
    
    /** 文档资源URL */
    private String documentUrl;
    
    /** PPT资源URL */
    private String pptUrl;
    
    /** 适用对象 */
    private String targetAudience;
    
    /** 前置要求 */
    private String prerequisite;
    
    /** 状态 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 审核意见 */
    private String auditRemark;
    
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
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
