package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 性别(0未知 1男 2女) */
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
    
    /** 状态(0禁用 1正常 2离职 3休假) */
    private Integer status;
    
    /** 最后登录IP */
    private String loginIp;
    
    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
    
    /** 密码过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pwdExpireTime;
    
    /** 锁定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockTime;
    
    /** 连续登录失败次数 */
    private Integer lockCount;
    
    @TableField(exist = false)
    private String deptName;
    
    @TableField(exist = false)
    private String positionName;
    
    @TableField(exist = false)
    private java.util.List<Role> roles;
}
