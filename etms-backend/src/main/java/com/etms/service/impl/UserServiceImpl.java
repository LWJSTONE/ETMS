package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.config.JwtTokenProvider;
import com.etms.dto.LoginDTO;
import com.etms.dto.UserDTO;
import com.etms.entity.Role;
import com.etms.entity.User;
import com.etms.entity.UserRole;
import com.etms.entity.Dept;
import com.etms.exception.BusinessException;
import com.etms.mapper.RoleMapper;
import com.etms.mapper.UserMapper;
import com.etms.mapper.UserRoleMapper;
import com.etms.mapper.DeptMapper;
import com.etms.service.CaptchaService;
import com.etms.service.UserService;
import com.etms.vo.LoginVO;
import com.etms.vo.RoleVO;
import com.etms.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.security.SecureRandom;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final DeptMapper deptMapper;
    private final CaptchaService captchaService;
    private final StringRedisTemplate stringRedisTemplate;
    
    // 登录失败锁定配置
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_DURATION_MINUTES = 30;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    @Override
    public LoginVO login(LoginDTO loginDTO, HttpServletRequest request) {
        // 验证验证码
        if (!captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha())) {
            throw new BusinessException("验证码错误或已过期");
        }
        
        // 获取用户信息
        User user = baseMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查账户是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            long remainingMinutes = java.time.Duration.between(
                LocalDateTime.now(), user.getLockTime()
            ).toMinutes();
            throw new BusinessException("账户已锁定，请在" + remainingMinutes + "分钟后重试");
        }
        
        // 检查账户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }
        
        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 登录成功，重置锁定计数和锁定时间
            resetLoginFailCount(user.getId());
            
            // 更新登录IP和登录时间
            updateLoginInfo(user.getId(), getClientIp(request));
            
            // 生成token
            String token = jwtTokenProvider.generateToken(authentication);
            
            // 获取角色和权限
            List<String> roles = baseMapper.selectRolesByUserId(user.getId());
            List<String> permissions = baseMapper.selectPermissionsByUserId(user.getId());
            
            // 构建返回对象
            LoginVO loginVO = new LoginVO();
            loginVO.setAccessToken(token);
            loginVO.setExpiresIn(jwtExpiration / 1000); // 转换为秒
            loginVO.setUserId(user.getId());
            loginVO.setUsername(user.getUsername());
            loginVO.setRealName(user.getRealName());
            loginVO.setAvatar(user.getAvatar());
            loginVO.setDeptName(user.getDeptName());
            loginVO.setRoles(roles);
            loginVO.setPermissions(permissions);
            
            return loginVO;
            
        } catch (BadCredentialsException e) {
            // 登录失败，增加失败计数
            handleLoginFail(user);
            throw new BusinessException("用户名或密码错误");
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("登录失败：" + e.getMessage());
        }
    }
    
    /**
     * 重置登录失败计数
     */
    private void resetLoginFailCount(Long userId) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setLockCount(0);
        updateUser.setLockTime(null);
        baseMapper.updateById(updateUser);
    }
    
    /**
     * 处理登录失败
     */
    private void handleLoginFail(User user) {
        int failCount = (user.getLockCount() == null ? 0 : user.getLockCount()) + 1;
        
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLockCount(failCount);
        
        // 超过最大失败次数，锁定账户
        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            updateUser.setLockTime(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
        }
        
        baseMapper.updateById(updateUser);
    }
    
    /**
     * 更新登录信息
     */
    private void updateLoginInfo(Long userId, String loginIp) {
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setLoginIp(loginIp);
        updateUser.setLoginTime(LocalDateTime.now());
        baseMapper.updateById(updateUser);
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多个代理的情况，第一个IP才是客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    @Override
    public void logout(HttpServletRequest request) {
        // 从请求头获取Token
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            
            // 将Token加入黑名单
            long remainingTime = jwtTokenProvider.getTokenRemainingTime(token);
            if (remainingTime > 0) {
                String key = TOKEN_BLACKLIST_PREFIX + token;
                stringRedisTemplate.opsForValue().set(key, "1", remainingTime, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        }
        
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
        
        // 修复N+1问题：批量查询用户角色
        List<Long> userIds = userPage.getRecords().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        // 批量查询所有用户的角色
        Map<Long, List<RoleVO>> userRoleMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            // 批量查询用户角色关联
            List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, userIds)
            );
            
            // 获取所有角色ID
            Set<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toSet());
            
            // 批量查询角色
            Map<Long, Role> roleMap = new HashMap<>();
            if (!roleIds.isEmpty()) {
                List<Role> roles = roleMapper.selectBatchIds(roleIds);
                roleMap = roles.stream()
                        .collect(Collectors.toMap(Role::getId, r -> r));
            }
            
            // 组装用户角色映射
            for (UserRole ur : userRoles) {
                Long userId = ur.getUserId();
                Role role = roleMap.get(ur.getRoleId());
                if (role != null) {
                    userRoleMap.computeIfAbsent(userId, k -> new ArrayList<>());
                    RoleVO roleVO = new RoleVO();
                    BeanUtils.copyProperties(role, roleVO);
                    userRoleMap.get(userId).add(roleVO);
                }
            }
        }
        
        // 批量查询部门名称
        Map<Long, String> deptNameMap = new HashMap<>();
        Set<Long> deptIds = userPage.getRecords().stream()
                .map(User::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!deptIds.isEmpty()) {
            List<Dept> depts = deptMapper.selectBatchIds(deptIds);
            deptNameMap = depts.stream()
                    .collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
        }
        
        final Map<Long, List<RoleVO>> finalUserRoleMap = userRoleMap;
        final Map<Long, String> finalDeptNameMap = deptNameMap;
        List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setGenderName(getGenderName(user.getGender()));
            vo.setStatusName(getStatusName(user.getStatus()));
            
            // 从批量查询结果中获取角色
            List<RoleVO> roleVOs = finalUserRoleMap.get(user.getId());
            if (!CollectionUtils.isEmpty(roleVOs)) {
                vo.setRoles(roleVOs);
            }
            
            // 设置部门名称
            if (user.getDeptId() != null) {
                vo.setDeptName(finalDeptNameMap.get(user.getDeptId()));
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
        
        // 查询并设置角色名称列表
        List<String> roles = baseMapper.selectRolesByUserId(id);
        vo.setRoleNames(roles);
        
        // 查询并设置权限列表
        List<String> permissions = baseMapper.selectPermissionsByUserId(id);
        vo.setPermissions(permissions);
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserDTO userDTO) {
        // 检查用户名是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getUsername, userDTO.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 如果前端传了密码则使用前端密码，否则使用默认密码
        String password = userDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            password = "123456"; // 默认密码
        }
        user.setPassword(passwordEncoder.encode(password));
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
        // 检查用户名是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDTO.getUsername())
                .ne(User::getId, userDTO.getId())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
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
        // 获取用户信息
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 禁止删除admin账户
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("admin账户不能删除");
        }
        
        // 检查用户是否有相关数据
        // 可以根据实际业务添加更多检查，如：培训记录、考试记录、签到记录等
        Long userRoleCount = userRoleMapper.selectCount(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id)
        );
        if (userRoleCount > 0) {
            // 删除用户角色关联
            baseMapper.deleteUserRoleByUserId(id);
        }
        
        // 删除用户
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        
        return baseMapper.updateById(updateUser) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 生成随机密码（8位，包含大小写字母和数字）
        String newPassword = generateRandomPassword(8);
        
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        
        baseMapper.updateById(updateUser);
        
        // TODO: 实际生产环境应通过邮件或短信发送新密码给用户
        // 这里记录日志，方便开发调试
        log.info("用户 {} 的密码已重置为: {}", user.getUsername(), newPassword);
    }
    
    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
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
        // 先删除原有角色关联
        baseMapper.deleteUserRoleByUserId(userId);
        
        if (CollectionUtils.isEmpty(roleIds)) {
            return true;
        }
        
        // 批量插入用户角色关联
        List<Map<String, Long>> userRoles = new ArrayList<>();
        for (Long roleId : roleIds) {
            Map<String, Long> map = new HashMap<>();
            map.put("userId", userId);
            map.put("roleId", roleId);
            userRoles.add(map);
        }
        
        // 使用批量插入
        baseMapper.batchInsertUserRole(userRoles);
        
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

    @Override
    public void exportUsers(UserDTO userDTO, HttpServletResponse response) {
        // 查询所有符合条件的用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(userDTO.getUsername()), User::getUsername, userDTO.getUsername())
               .like(StringUtils.hasText(userDTO.getRealName()), User::getRealName, userDTO.getRealName())
               .eq(userDTO.getStatus() != null, User::getStatus, userDTO.getStatus())
               .orderByDesc(User::getCreateTime);
        
        List<User> users = baseMapper.selectList(wrapper);
        
        // 批量查询部门名称
        Map<Long, String> deptNameMap = new HashMap<>();
        Set<Long> deptIds = users.stream()
                .map(User::getDeptId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!deptIds.isEmpty()) {
            List<Dept> depts = deptMapper.selectBatchIds(deptIds);
            deptNameMap = depts.stream()
                    .collect(Collectors.toMap(Dept::getId, Dept::getDeptName));
        }
        
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("用户数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new BusinessException("编码转换失败：" + e.getMessage());
        }
        
        // 构建CSV格式的数据（简单实现，实际项目建议使用EasyExcel或POI）
        StringBuilder sb = new StringBuilder();
        sb.append("用户名,真实姓名,性别,手机号,邮箱,部门,状态,创建时间\n");
        
        final Map<Long, String> finalDeptNameMap = deptNameMap;
        for (User user : users) {
            sb.append(user.getUsername()).append(",")
              .append(user.getRealName() != null ? user.getRealName() : "").append(",")
              .append(getGenderName(user.getGender())).append(",")
              .append(user.getPhone() != null ? user.getPhone() : "").append(",")
              .append(user.getEmail() != null ? user.getEmail() : "").append(",")
              .append(user.getDeptId() != null ? finalDeptNameMap.getOrDefault(user.getDeptId(), "") : "").append(",")
              .append(getStatusName(user.getStatus())).append(",")
              .append(user.getCreateTime() != null ? user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }
        
        try {
            response.getWriter().write(sb.toString());
        } catch (IOException e) {
            throw new BusinessException("导出用户数据失败：" + e.getMessage());
        }
    }
}
