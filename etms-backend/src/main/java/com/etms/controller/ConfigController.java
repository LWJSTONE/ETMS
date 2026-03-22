package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Config;
import com.etms.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

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
    
    @ApiOperation(value = "分页查询配置列表")
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
    @GetMapping("/{id}")
    public Result<Config> get(@PathVariable Long id) {
        Config config = configService.getConfigDetail(id);
        return Result.success(config);
    }
    
    @ApiOperation(value = "根据配置键名获取配置值")
    @GetMapping("/key/{configKey}")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        String value = configService.getConfigValue(configKey);
        return Result.success(value);
    }
    
    @ApiOperation(value = "新增配置")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Config config) {
        configService.addConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "更新配置")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Config config) {
        config.setId(id);
        configService.updateConfig(config);
        return Result.success();
    }
    
    @ApiOperation(value = "删除配置")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return Result.success();
    }
    
    @ApiOperation(value = "刷新缓存")
    @PostMapping("/refresh-cache")
    public Result<Void> refreshCache() {
        configService.refreshCache();
        return Result.success();
    }
}
