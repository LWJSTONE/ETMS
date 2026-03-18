package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 */
@Data
@TableName("sys_role_permission")
public class RolePermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 角色ID */
    private Long roleId;
    
    /** 权限ID */
    private Long permissionId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
