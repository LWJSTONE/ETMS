package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {
    
    /** 角色编码 */
    private String roleCode;
    
    /** 角色名称 */
    private String roleName;
    
    /** 角色描述 */
    private String roleDesc;
    
    /** 数据范围(1全部 2本部门 3本人) */
    private Integer dataScope;
    
    /** 排序顺序 */
    private Integer sortOrder;
    
    /** 状态(0禁用 1正常) */
    private Integer status;
    
    @TableField(exist = false)
    private java.util.List<Long> permissionIds;
}
