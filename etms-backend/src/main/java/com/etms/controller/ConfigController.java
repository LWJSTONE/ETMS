package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Config;
import com.etms.exception.BusinessException;
import com.etms.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统配置控制器
 */
@Api(tags = "系统配置管理")
@RestController
@RequestMapping("/system/configs")
@RequiredArgsConstructor
@Validated
public class ConfigController {
    
    private final ConfigService configService;
    
    // 敏感配置键名列表，这些配置不允许通过接口直接获取
    private static final Set<String> SENSITIVE_CONFIG_KEYS = new HashSet<>(Arrays.asList(
        "db.password", "redis.password", "jwt.secret", "email.password", 
        "sms.secret", "api.secret", "upload.secret", "system.secret"
    ));
    
    @ApiOperation(value = "分页查询配置列表")
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public Result<PageResult<Config>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
            @RequestParam(required = false) String configName,
            @RequestParam(required = false) String configKey,
            @RequestParam(required = false) Integer status) {
        Page<Config> page = new Page<>(current, size);
        Page<Config> resultPage = configService.pageConfigs(page, configName, configKey, status);
        PageResult<Config> pageResult = new PageResult<>(
                resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取配置详情")
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/{id}")
    public Result<Config> get(@PathVariable Long id) {
        Config config = configService.getConfigDetail(id);
        if (config == null) {
            throw new BusinessException("配置不存在");
        }
        return Result.success(config);
    }
    
    @ApiOperation(value = "根据配置键名获取配置值")
    @GetMapping("/key/{configKey}")
    @PreAuthorize("isAuthenticated()") // 修复：添加权限控制，要求用户已登录
    public Result<String> getConfigValue(@PathVariable String configKey) {
        // 安全检查：禁止获取敏感配置
        if (isSensitiveConfig(configKey)) {
            throw new BusinessException("无权获取该配置项");
        }
        String value = configService.getConfigValue(configKey);
        return Result.success(value);
    }
    
    @ApiOperation(value = "新增配置")
    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Config config) {
        configService.addConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "更新配置")
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Config config) {
        config.setId(id);
        configService.updateConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "删除配置")
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return Result.success();
    }
    
    @ApiOperation(value = "刷新缓存")
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        configService.refreshCache();
        return Result.success();
    }
    
    /**
     * 判断是否为敏感配置
     * 修复：增强敏感配置检测，防止通过大小写混合或特殊字符绕过
     */
    private boolean isSensitiveConfig(String configKey) {
        if (configKey == null) {
            return false;
        }
        // 修复：去除首尾空格并转小写后进行比较
        String lowerKey = configKey.trim().toLowerCase();
        
        // 检查是否在敏感配置列表中（精确匹配）
        for (String sensitiveKey : SENSITIVE_CONFIG_KEYS) {
            if (lowerKey.equals(sensitiveKey.toLowerCase())) {
                return true;
            }
        }
        
        // 修复：使用更严格的正则匹配敏感关键字
        // 检查是否包含password、secret、key等敏感词（作为独立单词）
        if (lowerKey.matches(".*[._-]?password[._-]?.*") ||
            lowerKey.matches(".*[._-]?secret[._-]?.*") ||
            lowerKey.matches(".*[._-]?private[_-]?key[._-]?.*") ||
            lowerKey.matches(".*[._-]?api[_-]?key[._-]?.*") ||
            lowerKey.matches(".*[._-]?access[_-]?key[._-]?.*") ||
            lowerKey.matches(".*[._-]?credential[s]?[._-]?.*")) {
            return true;
        }
        
        // 检查常见的敏感配置键名模式
        if (lowerKey.contains("password") || lowerKey.contains("secret") || 
            lowerKey.contains("privatekey") || lowerKey.contains("apikey")) {
            return true;
        }
        
        return false;
    }
}
