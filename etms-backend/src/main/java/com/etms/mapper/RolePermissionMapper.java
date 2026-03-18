package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}
