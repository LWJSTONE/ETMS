package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_position")
public class Position extends BaseEntity {
    
    /** 岗位名称 */
    private String positionName;
    
    /** 岗位编码 */
    private String positionCode;
    
    /** 职级 */
    private String positionLevel;
    
    /** 岗位职责描述 */
    private String positionDesc;
    
    /** 任职要求 */
    private String requirement;
    
    /** 所属部门ID */
    private Long deptId;
    
    /** 排序 */
    private Integer sortOrder;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
    // 注：remark字段已在BaseEntity中定义，无需重复声明
}
