package com.etms.security;

import com.etms.entity.User;
import com.etms.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户详情服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 状态检查：处理status为null的情况
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用或已离职");
        }
        
        // 获取用户角色
        List<String> roles = userMapper.selectRolesByUserId(user.getId());
        
        // 获取用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        
        // 构建权限列表：角色添加ROLE_前缀，权限直接使用
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // 添加角色（添加ROLE_前缀）
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        
        // 添加权限
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        
        // 使用自定义LoginUser，包含用户ID
        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
