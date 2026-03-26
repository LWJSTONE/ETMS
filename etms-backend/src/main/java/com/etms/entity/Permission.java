package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限实体类
 * 注：sys_permission表只有id, perm_code, perm_name, perm_type, parent_id, path, icon, 
 * component, sort_order, visible, status, create_time, update_time, deleted字段
 */
@Data
@TableName("sys_permission")
public class Permission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
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
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    
    /** 逻辑删除标志(0未删除 1已删除) */
    @TableLogic
    private Integer deleted;
    
    /** 子权限列表 */
    @TableField(exist = false)
    private List<Permission> children;
}
