import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

/**
 * 权限指令 v-permission
 * 用法: v-permission="['permission:code']" 或 v-permission="'permission:code'"
 * 当用户没有指定权限时，元素会被移除
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const value = binding.value
    
    if (!value) return
    
    // 获取需要检查的权限列表
    const requiredPermissions: string[] = Array.isArray(value) ? value : [value]
    
    if (requiredPermissions.length === 0) return
    
    const userStore = useUserStore()
    const userPermissions = userStore.userInfo?.permissions || []
    const roleNames = userStore.userInfo?.roleNames || []
    
    // 检查是否为管理员角色（管理员拥有所有权限）
    const isAdmin = roleNames.some(name => 
      name === '超级管理员' || name === '管理员' || name.toLowerCase() === 'admin'
    )
    
    if (isAdmin) return
    
    // 检查用户是否拥有任一所需权限
    const hasPermission = requiredPermissions.some(permission => {
      // 支持通配符匹配
      // 例如：system:* 可以匹配 system:user:view
      return userPermissions.some(userPerm => {
        if (userPerm === permission) return true
        if (userPerm === '*') return true
        if (userPerm.endsWith(':*')) {
          const prefix = userPerm.slice(0, -1)
          return permission.startsWith(prefix)
        }
        return false
      })
    })
    
    // 如果没有权限，移除元素
    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  }
}

/**
 * 角色指令 v-role
 * 用法: v-role="['admin']" 或 v-role="'admin'"
 * 当用户没有指定角色时，元素会被移除
 */
export const role: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const value = binding.value
    
    if (!value) return
    
    // 获取需要检查的角色列表
    const requiredRoles: string[] = Array.isArray(value) ? value : [value]
    
    if (requiredRoles.length === 0) return
    
    const userStore = useUserStore()
    const userRoles = userStore.userInfo?.roleNames || []
    
    // 检查用户是否拥有任一所需角色（忽略大小写）
    const hasRole = userRoles.some(userRole => 
      requiredRoles.some(requiredRole => 
        userRole.toLowerCase() === requiredRole.toLowerCase() ||
        userRole === '超级管理员' ||
        userRole === '管理员' ||
        userRole.toLowerCase() === 'admin'
      )
    )
    
    // 如果没有角色，移除元素
    if (!hasRole) {
      el.parentNode?.removeChild(el)
    }
  }
}

export default {
  permission,
  role
}
