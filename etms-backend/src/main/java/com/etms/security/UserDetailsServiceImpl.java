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
import java.util.stream.Collectors;

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
        
        if (user.getStatus() != 1) {
            throw new UsernameNotFoundException("用户已被禁用或已离职");
        }
        
        // 获取用户角色
        List<String> roles = userMapper.selectRolesByUserId(user.getId());
        
        // 获取用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        
        // 合并角色和权限
        roles.addAll(permissions);
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
