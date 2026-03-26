package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Config;
import com.etms.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
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
    @PreAuthorize("hasAuthority('system:config:list')")
    @GetMapping
    public Result<PageResult<Config>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
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
    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping("/{id}")
    public Result<Config> get(@PathVariable Long id) {
        Config config = configService.getConfigDetail(id);
        if (config == null) {
            return Result.error("配置不存在");
        }
        return Result.success(config);
    }
    
    @ApiOperation(value = "根据配置键名获取配置值")
    @GetMapping("/key/{configKey}")
    @PreAuthorize("isAuthenticated()") // 修复：添加权限控制，要求用户已登录
    public Result<String> getConfigValue(@PathVariable String configKey) {
        // 安全检查：禁止获取敏感配置
        if (isSensitiveConfig(configKey)) {
            return Result.error("无权获取该配置项");
        }
        String value = configService.getConfigValue(configKey);
        return Result.success(value);
    }
    
    @ApiOperation(value = "新增配置")
    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Config config) {
        configService.addConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "更新配置")
    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Config config) {
        config.setId(id);
        configService.updateConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "删除配置")
    @PreAuthorize("hasAuthority('system:config:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return Result.success();
    }
    
    @ApiOperation(value = "刷新缓存")
    @PreAuthorize("hasAuthority('system:config:cache')")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        configService.refreshCache();
        return Result.success();
    }
    
    /**
     * 判断是否为敏感配置
     */
    private boolean isSensitiveConfig(String configKey) {
        if (configKey == null) {
            return false;
        }
        String lowerKey = configKey.toLowerCase();
        // 检查是否在敏感配置列表中
        if (SENSITIVE_CONFIG_KEYS.contains(configKey)) {
            return true;
        }
        // 检查是否包含敏感关键字
        return lowerKey.contains("password") || lowerKey.contains("secret") || lowerKey.contains("key");
    }
}
