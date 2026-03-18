package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

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
     * 根据用户ID查询用户角色
     */
    List<String> selectRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询用户权限
     */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
