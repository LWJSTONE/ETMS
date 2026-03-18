package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Role;
import com.etms.vo.RoleVO;
import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 分页查询角色列表
     */
    Page<RoleVO> pageRoles(Page<Role> page, String roleName, Integer status);
    
    /**
     * 获取所有角色列表
     */
    List<RoleVO> listRoles();
    
    /**
     * 获取角色详情
     */
    RoleVO getRoleDetail(Long id);
    
    /**
     * 新增角色
     */
    void addRole(Role role);
    
    /**
     * 更新角色
     */
    void updateRole(Role role);
    
    /**
     * 删除角色
     */
    void deleteRole(Long id);
    
    /**
     * 分配权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
    
    /**
     * 获取角色权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
}
