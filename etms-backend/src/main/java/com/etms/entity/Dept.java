package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class Dept extends BaseEntity {
    
    /** 父部门ID */
    private Long parentId;
    
    /** 部门名称 */
    private String deptName;
    
    /** 部门编码 */
    private String deptCode;
    
    /** 部门负责人ID */
    private Long leaderId;
    
    /** 排序顺序 */
    private Integer sortOrder;
    
    /** 部门层级 */
    private Integer level;
    
    /** 祖级列表 */
    private String ancestors;
    
    /** 状态(0禁用 1正常) */
    private Integer status;
    
    @TableField(exist = false)
    private String leaderName;
    
    @TableField(exist = false)
    private java.util.List<Dept> children;
}
