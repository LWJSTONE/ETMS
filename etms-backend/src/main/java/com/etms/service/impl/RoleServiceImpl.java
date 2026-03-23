package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Role;
import com.etms.entity.RolePermission;
import com.etms.entity.Permission;
import com.etms.entity.UserRole;
import com.etms.exception.BusinessException;
import com.etms.mapper.RoleMapper;
import com.etms.mapper.PermissionMapper;
import com.etms.mapper.UserRoleMapper;
import com.etms.service.RolePermissionService;
import com.etms.service.RoleService;
import com.etms.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionService rolePermissionService;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public Page<RoleVO> pageRoles(Page<Role> page, String roleName, Integer status) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(roleName), Role::getRoleName, roleName)
               .eq(status != null, Role::getStatus, status)
               .orderByAsc(Role::getSortOrder);

        Page<Role> rolePage = baseMapper.selectPage(page, wrapper);

        Page<RoleVO> voPage = new Page<>();
        BeanUtils.copyProperties(rolePage, voPage, "records");

        // 修复N+1查询问题：批量查询权限数量
        List<Long> roleIds = rolePage.getRecords().stream()
                .map(Role::getId)
                .collect(Collectors.toList());
        
        // 批量查询每个角色的权限数量
        Map<Long, Long> permissionCountMap = new java.util.HashMap<>();
        if (!roleIds.isEmpty()) {
            List<RolePermission> rolePermissions = rolePermissionService.list(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds)
            );
            // 统计每个角色的权限数量
            permissionCountMap = rolePermissions.stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId, Collectors.counting()));
        }

        final Map<Long, Long> finalPermissionCountMap = permissionCountMap;
        List<RoleVO> voList = rolePage.getRecords().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            // 从批量查询结果中获取权限数量
            vo.setPermissionCount(finalPermissionCountMap.getOrDefault(role.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<RoleVO> listRoles() {
        List<Role> roles = baseMapper.selectList(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getSortOrder)
        );

        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        Role role = baseMapper.selectById(id);
        if (role == null) {
            return null;
        }
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(Role role) {
        // 检查角色名是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Role>().eq(Role::getRoleName, role.getRoleName())
        );
        if (count > 0) {
            throw new BusinessException("角色名已存在");
        }

        // 检查角色编码是否重复
        if (role.getRoleCode() != null) {
            Long codeCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, role.getRoleCode())
            );
            if (codeCount > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }

        role.setStatus(1);
        baseMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Role role) {
        // 检查角色名是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleName, role.getRoleName())
                .ne(Role::getId, role.getId())
        );
        if (count > 0) {
            throw new BusinessException("角色名已存在");
        }

        // 检查角色编码是否重复（排除自身）
        if (role.getRoleCode() != null) {
            Long codeCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleCode, role.getRoleCode())
                    .ne(Role::getId, role.getId())
            );
            if (codeCount > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }

        baseMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 获取角色信息
        Role role = baseMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 修复：禁止删除系统内置角色（ADMIN角色）
        if ("ADMIN".equals(role.getRoleCode()) || "admin".equals(role.getRoleCode())) {
            throw new BusinessException("系统内置角色不能删除");
        }
        
        // 检查角色是否已分配给用户
        Long count = userRoleMapper.selectCount(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id)
        );
        if (count > 0) {
            throw new BusinessException("角色已分配给用户，无法删除。请先解除用户与该角色的关联。");
        }

        // 删除角色权限关联
        rolePermissionService.remove(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id)
        );
        
        // 修复问题11：清理其他可能的关联数据
        // 注意：UserRole关联已在上方检查，如果有用户关联则不允许删除
        
        // 删除角色
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有关联
        rolePermissionService.remove(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId)
        );

        // 批量插入权限记录
        if (permissionIds != null && !permissionIds.isEmpty()) {
            // 自动补充父权限
            Set<Long> allPermissionIds = new java.util.HashSet<>(permissionIds);
            for (Long permissionId : permissionIds) {
                addParentPermissions(permissionId, allPermissionIds);
            }
            
            List<RolePermission> rolePermissions = new ArrayList<>();
            for (Long permissionId : allPermissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissions.add(rp);
            }
            // 使用 MyBatis-Plus 的批量插入方法
            rolePermissionService.saveBatch(rolePermissions);
        }
    }
    
    /**
     * 递归添加父权限
     * @param permissionId 当前权限ID
     * @param allPermissionIds 所有权限ID集合
     */
    private void addParentPermissions(Long permissionId, Set<Long> allPermissionIds) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null || permission.getParentId() == null || permission.getParentId() == 0) {
            return;
        }
        Long parentId = permission.getParentId();
        if (!allPermissionIds.contains(parentId)) {
            allPermissionIds.add(parentId);
            // 递归查找父权限的父权限
            addParentPermissions(parentId, allPermissionIds);
        }
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        List<RolePermission> list = rolePermissionService.list(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId)
        );
        return list.stream()
            .map(RolePermission::getPermissionId)
            .collect(Collectors.toList());
    }
}
