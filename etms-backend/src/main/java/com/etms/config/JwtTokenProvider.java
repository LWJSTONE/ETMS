package com.etms.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token提供者
 */
@Slf4j
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    /**
     * 修复：在应用启动时验证JWT密钥配置
     * 避免运行时才发现密钥配置错误导致生产事故
     */
    @PostConstruct
    public void validateConfiguration() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置，请在配置文件中设置jwt.secret属性");
        }
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT密钥长度不足，必须至少32字节（256位），当前长度：" + keyBytes.length + "。请在配置文件中设置更长的jwt.secret值");
        }
        log.info("JWT配置验证通过，密钥长度: {}字节", keyBytes.length);
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        // 校验密钥长度，HS256要求至少32字节（256位）
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT密钥长度不足，必须至少32字节（256位），当前长度：" + keyBytes.length);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * 生成Token
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从Token获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * 验证Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT类型: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT参数非法: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT验证异常: {}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取Token的剩余有效时间（毫秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining > 0 ? remaining : 0;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("获取Token剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }
}
