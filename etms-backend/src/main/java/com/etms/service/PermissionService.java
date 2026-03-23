package com.etms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Permission;
import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {
    
    /**
     * 获取权限树形结构
     * @return 权限树列表
     */
    List<Permission> getPermissionTree();
}
