package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {
    
    /** 权限编码 */
    private String permCode;
    
    /** 权限名称 */
    private String permName;
    
    /** 权限类型(1菜单 2按钮 3接口) */
    private Integer permType;
    
    /** 父权限ID */
    private Long parentId;
    
    /** 路由路径 */
    private String path;
    
    /** 菜单图标 */
    private String icon;
    
    /** 前端组件路径 */
    private String component;
    
    /** 排序顺序 */
    private Integer sortOrder;
    
    /** 是否可见(0隐藏 1显示) */
    private Integer visible;
    
    /** 状态(0禁用 1正常) */
    private Integer status;
    
    /** 逻辑删除标志(0未删除 1已删除) */
    @TableLogic
    private Integer deleted;
    
    @TableField(exist = false)
    private java.util.List<Permission> children;
}
