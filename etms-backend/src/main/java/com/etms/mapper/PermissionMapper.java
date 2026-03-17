package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询权限树形结构
     */
    List<Permission> selectPermissionTree();
}
