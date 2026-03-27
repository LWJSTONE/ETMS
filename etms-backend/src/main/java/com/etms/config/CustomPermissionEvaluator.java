package com.etms.config;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自定义权限评估器
 * 实现 hasPermission 表达式，检查用户是否拥有指定的权限
 * 
 * 支持两种权限检查方式：
 * 1. 完全匹配：hasPermission('system:user', 'read') - 检查用户是否有 'system:user' 权限
 * 2. 字符串匹配：hasPermission('system:user') - 直接检查用户是否有该权限字符串
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        String targetDomain = targetDomainObject != null ? targetDomainObject.toString() : null;
        
        // 如果提供了目标域对象，构建完整权限字符串
        // 例如：hasPermission('system:user', 'read') 会检查用户是否有 'system:user' 权限
        if (targetDomain != null) {
            // 检查用户是否拥有该模块的权限（前缀匹配）
            // 例如：用户有 'system:user' 权限，则可以通过 hasPermission('system:user', 'read') 检查
            return checkPermission(authentication, targetDomain);
        }
        
        // 直接检查权限字符串
        return checkPermission(authentication, permissionString);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        
        String permissionString = permission.toString();
        
        // 如果提供了目标类型，检查该类型的权限
        if (targetType != null) {
            return checkPermission(authentication, targetType);
        }
        
        return checkPermission(authentication, permissionString);
    }
    
    /**
     * 检查用户是否拥有指定权限
     * 支持精确匹配和前缀匹配
     */
    private boolean checkPermission(Authentication authentication, String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authAuthority = authority.getAuthority();
            
            // 精确匹配
            if (permission.equals(authAuthority)) {
                return true;
            }
            
            // 前缀匹配：如果用户有 'system:user' 权限，则可以通过 'system:user:*' 或 'system:user' 检查
            // 同时也支持：用户有 'system:user' 时，可以访问需要 'system:user:list' 等子权限的资源
            if (authAuthority.equals(permission) || 
                authAuthority.startsWith(permission + ":") ||
                permission.startsWith(authAuthority + ":")) {
                return true;
            }
        }
        
        return false;
    }
}
