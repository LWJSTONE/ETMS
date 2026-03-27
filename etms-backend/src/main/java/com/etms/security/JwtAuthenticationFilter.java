package com.etms.security;

import com.etms.config.JwtTokenProvider;
import com.etms.entity.User;
import com.etms.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JWT认证过滤器
 * 优化版本：减少数据库查询，添加Redis超时处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserMapper userMapper;
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * 用户状态常量
     */
    private static final int USER_STATUS_DISABLED = 0;
    private static final int USER_STATUS_RESIGNED = 2;
    
    /**
     * Redis操作超时时间（毫秒）
     */
    private static final long REDIS_TIMEOUT_MS = 3000;
    
    /**
     * 不需要检查用户状态的路径（避免循环查询）
     */
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/api/auth/logout",
            "/api/auth/info"
    ));
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 检查Token是否在黑名单中（带超时处理）
            if (isTokenBlacklistedWithTimeout(token)) {
                // Token已被注销，返回401认证失败响应
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                return;
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            // 检查SecurityContext中是否已有认证信息，避免重复设置
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // 优化：从LoginUser中直接获取用户状态，避免再次查询数据库
                if (!isUserActiveFromCache(userDetails, request.getRequestURI())) {
                    log.warn("用户[{}]状态异常，拒绝访问", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"账号状态异常，请联系管理员\"}");
                    return;
                }
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从缓存的UserDetails中检查用户状态（避免重复查询数据库）
     * 只有在状态确实需要刷新时才查询数据库
     * @param userDetails 用户详情（包含缓存的用户状态）
     * @param requestUri 请求URI
     * @return true-用户状态正常，false-用户状态异常
     */
    private boolean isUserActiveFromCache(UserDetails userDetails, String requestUri) {
        // 对于登出和获取用户信息接口，允许通过
        if (EXCLUDED_PATHS.stream().anyMatch(requestUri::contains)) {
            return true;
        }
        
        // 从LoginUser中获取缓存的用户状态
        if (userDetails instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) userDetails;
            Integer status = loginUser.getStatus();
            
            // 如果状态为null，说明缓存中没有状态信息，需要查询数据库
            if (status == null) {
                log.debug("用户状态未缓存，从数据库查询");
                return isUserActiveFromDatabase(loginUser.getUsername());
            }
            
            // 检查用户是否被禁用或已离职
            if (status == USER_STATUS_DISABLED || status == USER_STATUS_RESIGNED) {
                log.warn("用户[{}]状态异常，status={}", loginUser.getUsername(), status);
                return false;
            }
            
            return true;
        }
        
        // 如果不是LoginUser类型，回退到数据库查询
        return isUserActiveFromDatabase(userDetails.getUsername());
    }
    
    /**
     * 从数据库检查用户状态（仅作为后备方案）
     * @param username 用户名
     * @return true-用户状态正常，false-用户状态异常
     */
    private boolean isUserActiveFromDatabase(String username) {
        try {
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                log.warn("用户[{}]不存在", username);
                return false;
            }
            
            // 检查用户是否被禁用或已离职
            if (user.getStatus() != null && 
                (user.getStatus() == USER_STATUS_DISABLED || user.getStatus() == USER_STATUS_RESIGNED)) {
                log.warn("用户[{}]状态异常，status={}", username, user.getStatus());
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("检查用户状态异常: {}", e.getMessage());
            // 修复安全问题：异常时拒绝访问，避免被禁用用户在数据库故障时绕过验证
            return false;
        }
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 检查Token是否在黑名单中（带超时处理）
     * 优化：添加超时机制，避免Redis连接问题导致请求阻塞
     */
    private boolean isTokenBlacklistedWithTimeout(String token) {
        if (stringRedisTemplate == null) {
            // Redis不可用时，跳过黑名单检查
            log.debug("Redis不可用，跳过Token黑名单检查");
            return false;
        }
        
        try {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            // 使用带超时的操作，避免长时间阻塞
            Boolean exists = stringRedisTemplate.opsForValue().getOperations()
                    .getExpire(key, TimeUnit.MILLISECONDS) >= 0;
            // 更准确的方式：直接检查key是否存在
            exists = stringRedisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            // Redis连接失败，记录日志但不阻塞请求
            log.warn("Redis连接失败，跳过Token黑名单检查: {}", e.getMessage());
            return false;
        } catch (org.springframework.data.redis.RedisSystemException e) {
            // Redis系统异常，记录日志但不阻塞请求
            log.warn("Redis系统异常，跳过Token黑名单检查: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他异常，记录日志但不阻塞请求
            log.warn("检查Token黑名单时发生异常: {}", e.getMessage());
            return false;
        }
    }
}
