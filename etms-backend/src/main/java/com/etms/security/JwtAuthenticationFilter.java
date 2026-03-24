package com.etms.security;

import com.etms.config.JwtTokenProvider;
import com.etms.entity.User;
import com.etms.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    /**
     * 用户状态常量
     */
    private static final int USER_STATUS_DISABLED = 0;
    private static final int USER_STATUS_RESIGNED = 2;
    
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
            // 检查Token是否在黑名单中
            if (isTokenBlacklisted(token)) {
                // Token已被注销，返回401认证失败响应
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                return;
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(token);
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 检查用户状态（防止被禁用/离职用户继续使用有效Token）
            if (!isUserActive(username, request.getRequestURI())) {
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
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 检查用户状态是否正常
     * @param username 用户名
     * @param requestUri 请求URI
     * @return true-用户状态正常，false-用户状态异常
     */
    private boolean isUserActive(String username, String requestUri) {
        // 对于登出和获取用户信息接口，允许通过（避免前端无法获取状态信息）
        if (EXCLUDED_PATHS.stream().anyMatch(requestUri::contains)) {
            return true;
        }
        
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
            // 出现异常时允许请求通过，由后续处理决定
            return true;
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
     * 检查Token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
