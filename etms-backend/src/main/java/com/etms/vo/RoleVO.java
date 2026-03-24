package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色响应VO
 */
@Data
public class RoleVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 角色ID */
    private Long id;
    
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
    
    /** 权限数量 */
    private Long permissionCount;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
