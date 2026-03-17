package com.etms.vo;

import lombok.Data;
import java.io.Serializable;

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
}
