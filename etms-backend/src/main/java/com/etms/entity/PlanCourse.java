package com.etms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 培训计划课程关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("etms_plan_course")
public class PlanCourse extends BaseEntity {
    
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
}
