package com.etms.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户请求DTO
 */
@Data
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 性别 */
    private Integer gender;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像URL */
    private String avatar;
    
    /** 部门ID */
    private Long deptId;
    
    /** 岗位ID */
    private Long positionId;
    
    /** 状态 */
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    /** 角色ID列表 */
    private List<Long> roleIds;
}
