package com.etms.config;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自定义权限评估器
 * 实现 hasPermission 表达式，检查用户是否拥有指定的权限
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        
        // 检查用户是否拥有该权限
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (permissionString.equals(authority.getAuthority())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // 对于基于ID和类型的权限检查，简化处理为直接检查权限字符串
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        
        // 检查用户是否拥有该权限
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (permissionString.equals(authority.getAuthority())) {
                return true;
            }
        }
        
        return false;
    }
}
