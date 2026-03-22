package com.etms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.RolePermission;
import com.etms.mapper.RolePermissionMapper;
import com.etms.service.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色权限关联服务实现类
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
