package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Permission;
import com.etms.mapper.PermissionMapper;
import com.etms.service.PermissionService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    
    @Override
    public List<Permission> getPermissionTree() {
        // 查询所有状态为正常的权限
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getSortOrder);
        List<Permission> allPermissions = list(wrapper);
        
        // 构建树形结构
        return buildPermissionTree(allPermissions, 0L);
    }
    
    /**
     * 递归构建权限树
     * 修复：添加ID空值检查，防止递归时出现空指针异常
     * @param allPermissions 所有权限列表
     * @param parentId 父ID
     * @return 权限树
     */
    private List<Permission> buildPermissionTree(List<Permission> allPermissions, Long parentId) {
        List<Permission> tree = new ArrayList<>();
        
        if (allPermissions == null || allPermissions.isEmpty()) {
            return tree;
        }
        
        for (Permission permission : allPermissions) {
            // 修复：检查权限对象的ID是否为空，避免递归时传入null
            if (permission.getId() == null) {
                continue;
            }
            
            if ((parentId == null && permission.getParentId() == null) ||
                (parentId != null && parentId.equals(permission.getParentId()))) {
                // 递归构建子节点，此时permission.getId()一定不为null
                permission.setChildren(buildPermissionTree(allPermissions, permission.getId()));
                tree.add(permission);
            }
        }
        
        return tree;
    }
}
