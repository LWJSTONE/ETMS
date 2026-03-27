package com.etms.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Redis配置类
 * 优化连接超时设置，避免Redis不可用时阻塞请求
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private int port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private int database;

    /**
     * 配置Redis连接工厂
     * 设置合理的连接超时和读写超时，避免阻塞
     */
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public LettuceConnectionFactory redisConnectionFactory() {
        // 配置Redis服务器信息
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(database);
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }

        // 配置Socket选项 - 设置连接超时
        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(3))  // 连接超时3秒
                .keepAlive(true)
                .build();

        // 配置客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)  // 自动重连
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)  // 断开时拒绝命令
                .build();

        // 配置Lettuce客户端
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(3))  // 命令超时3秒
                .clientOptions(clientOptions)
                .build();

        log.info("Redis连接配置: host={}, port={}, database={}, connectTimeout=3s, commandTimeout=3s", 
                host, port, database);

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * StringRedisTemplate Bean
     * 如果Redis连接失败，返回一个安全的模板（不会阻塞）
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        log.info("StringRedisTemplate初始化完成");
        return template;
    }
}
