package com.etms.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 登录响应VO
 */
@Data
public class LoginVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 访问令牌 */
    private String accessToken;
    
    /** 令牌类型 */
    private String tokenType = "Bearer";
    
    /** 过期时间(秒) */
    private Long expiresIn;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 头像 */
    private String avatar;
    
    /** 部门名称 */
    private String deptName;
    
    /** 角色列表 */
    private List<String> roles;
    
    /** 权限列表 */
    private List<String> permissions;
}
