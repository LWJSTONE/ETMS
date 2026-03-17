package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.config.JwtTokenProvider;
import com.etms.dto.LoginDTO;
import com.etms.dto.UserDTO;
import com.etms.entity.Role;
import com.etms.entity.User;
import com.etms.mapper.RoleMapper;
import com.etms.mapper.UserMapper;
import com.etms.service.UserService;
import com.etms.vo.LoginVO;
import com.etms.vo.RoleVO;
import com.etms.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成token
        String token = jwtTokenProvider.generateToken(authentication);
        
        // 获取用户信息
        User user = baseMapper.selectByUsername(loginDTO.getUsername());
        
        // 获取角色和权限
        List<String> roles = baseMapper.selectRolesByUserId(user.getId());
        List<String> permissions = baseMapper.selectPermissionsByUserId(user.getId());
        
        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(token);
        loginVO.setExpiresIn(86400L);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setDeptName(user.getDeptName());
        loginVO.setRoles(roles);
        loginVO.setPermissions(permissions);
        
        return loginVO;
    }
    
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
    
    @Override
    public Page<UserVO> pageUsers(Page<User> page, UserDTO userDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userDTO.getUsername()), User::getUsername, userDTO.getUsername())
               .like(StringUtils.hasText(userDTO.getRealName()), User::getRealName, userDTO.getRealName())
               .like(StringUtils.hasText(userDTO.getPhone()), User::getPhone, userDTO.getPhone())
               .eq(userDTO.getDeptId() != null, User::getDeptId, userDTO.getDeptId())
               .eq(userDTO.getStatus() != null, User::getStatus, userDTO.getStatus())
               .orderByDesc(User::getCreateTime);
        
        Page<User> userPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<UserVO> voPage = new Page<>();
        BeanUtils.copyProperties(userPage, voPage, "records");
        
        List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setGenderName(getGenderName(user.getGender()));
            vo.setStatusName(getStatusName(user.getStatus()));
            
            // 查询角色
            List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
            if (!CollectionUtils.isEmpty(roles)) {
                List<RoleVO> roleVOs = roles.stream().map(role -> {
                    RoleVO roleVO = new RoleVO();
                    BeanUtils.copyProperties(role, roleVO);
                    return roleVO;
                }).collect(Collectors.toList());
                vo.setRoles(roleVOs);
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public UserVO getUserDetail(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            return null;
        }
        
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setGenderName(getGenderName(user.getGender()));
        vo.setStatusName(getStatusName(user.getStatus()));
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setStatus(1);
        
        int result = baseMapper.insert(user);
        
        // 分配角色
        if (!CollectionUtils.isEmpty(userDTO.getRoleIds())) {
            assignRoles(user.getId(), userDTO.getRoleIds());
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        
        int result = baseMapper.updateById(user);
        
        // 更新角色
        if (!CollectionUtils.isEmpty(userDTO.getRoleIds())) {
            // 先删除原有角色
            baseMapper.deleteUserRoleByUserId(userDTO.getId());
            // 再分配新角色
            assignRoles(userDTO.getId(), userDTO.getRoleIds());
        }
        
        return result > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 删除用户角色关联
        baseMapper.deleteUserRoleByUserId(id);
        // 删除用户
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        
        return baseMapper.updateById(updateUser) > 0;
    }
    
    @Override
    public boolean resetPassword(Long userId) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode("123456"));
        
        return baseMapper.updateById(updateUser) > 0;
    }
    
    @Override
    public boolean updateStatus(Long userId, Integer status) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setStatus(status);
        
        return baseMapper.updateById(updateUser) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return true;
        }
        
        // 批量插入用户角色关联
        for (Long roleId : roleIds) {
            baseMapper.insertUserRole(userId, roleId);
        }
        
        return true;
    }
    
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        return baseMapper.selectByUsername(username);
    }
    
    private String getGenderName(Integer gender) {
        if (gender == null) return "未知";
        switch (gender) {
            case 1: return "男";
            case 2: return "女";
            default: return "未知";
        }
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "禁用";
            case 1: return "正常";
            case 2: return "离职";
            case 3: return "休假";
            default: return "未知";
        }
    }
}
