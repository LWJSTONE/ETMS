package com.etms.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * 登录用户身份权限
 * 用于存储在SecurityContext中的用户信息
 */
@Data
public class LoginUser implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 权限列表 */
    private Collection<? extends GrantedAuthority> authorities;
    
    /** 账户是否未过期 */
    private boolean accountNonExpired = true;
    
    /** 账户是否未锁定 */
    private boolean accountNonLocked = true;
    
    /** 凭证是否未过期 */
    private boolean credentialsNonExpired = true;
    
    /** 账户是否启用 */
    private boolean enabled = true;
    
    /** 用户状态（0-禁用，1-正常，2-离职）- 用于快速检查用户状态，避免重复查询数据库 */
    private Integer status;
    
    public LoginUser() {
    }
    
    public LoginUser(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }
    
    public LoginUser(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities, Integer status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.status = status;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
