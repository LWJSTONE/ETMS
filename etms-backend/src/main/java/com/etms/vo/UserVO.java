package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户响应VO
 */
@Data
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 性别 */
    private Integer gender;
    
    /** 性别名称 */
    private String genderName;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像URL */
    private String avatar;
    
    /** 部门ID */
    private Long deptId;
    
    /** 部门名称 */
    private String deptName;
    
    /** 岗位ID */
    private Long positionId;
    
    /** 岗位名称 */
    private String positionName;
    
    /** 状态 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 最后登录IP */
    private String loginIp;
    
    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /** 备注 */
    private String remark;
    
    /** 角色列表 */
    private List<RoleVO> roles;
}
