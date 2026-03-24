package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.User;
import com.etms.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询用户角色名称列表
     */
    List<String> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询用户角色详情列表（包含id等信息）
     */
    List<Role> selectRoleDetailsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询用户权限
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 删除用户角色关联
     */
    int deleteUserRoleByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户角色关联
     */
    int batchInsertUserRole(@Param("list") List<Map<String, Long>> list);
    
    /**
     * 原子性地增加登录失败计数
     * 修复并发问题：使用数据库原子操作
     * @param userId 用户ID
     * @return 影响的行数
     */
    int incrementLockCount(@Param("userId") Long userId);
}
