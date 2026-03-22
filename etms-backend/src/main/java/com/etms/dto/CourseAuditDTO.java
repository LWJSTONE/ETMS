package com.etms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 课程审核DTO
 */
@Data
public class CourseAuditDTO {
    
    /**
     * 审核状态(2已上架 4审核驳回)
     */
    @NotNull(message = "审核状态不能为空")
    private Integer status;
    
    /**
     * 审核意见
     */
    private String auditRemark;
}
