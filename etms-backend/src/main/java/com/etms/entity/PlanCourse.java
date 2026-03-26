package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 培训计划课程关联实体
 * 注：etms_plan_course表只有id, plan_id, course_id, sort_order, required, 
 * create_by, create_time, update_by, update_time, remark字段，没有deleted字段
 */
@Data
@TableName("etms_plan_course")
public class PlanCourse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 培训计划ID
     */
    private Long planId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程排序
     */
    private Integer sortOrder;
    
    /**
     * 是否必修：0-选修 1-必修
     */
    private Integer required;
    
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
