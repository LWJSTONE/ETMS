package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Config;
import com.etms.exception.BusinessException;
import com.etms.mapper.ConfigMapper;
import com.etms.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统配置服务实现类
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {
    
    /** 配置缓存 */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    @Override
    public Page<Config> pageConfigs(Page<Config> page, String configName, String configKey, Integer status) {
        LambdaQueryWrapper<Config> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(configName), Config::getConfigName, configName)
               .like(StringUtils.hasText(configKey), Config::getConfigKey, configKey)
               .eq(status != null, Config::getStatus, status)
               .orderByDesc(Config::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Config getConfigDetail(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public String getConfigValue(String configKey) {
        // 先从缓存获取
        if (configCache.containsKey(configKey)) {
            return configCache.get(configKey);
        }
        
        // 从数据库获取
        Config config = baseMapper.selectOne(
            new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, configKey)
        );
        
        if (config != null) {
            configCache.put(configKey, config.getConfigValue());
            return config.getConfigValue();
        }
        return null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(Config config) {
        // 检查配置键名是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Config>().eq(Config::getConfigKey, config.getConfigKey())
        );
        if (count > 0) {
            throw new BusinessException("配置键名已存在");
        }
        
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getConfigType() == null) {
            config.setConfigType(2); // 默认业务配置
        }
        
        baseMapper.insert(config);
        // 更新缓存
        configCache.put(config.getConfigKey(), config.getConfigValue());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Config config) {
        // 获取原配置信息
        Config oldConfig = baseMapper.selectById(config.getId());
        if (oldConfig == null) {
            throw new BusinessException("配置不存在");
        }
        
        // 检查配置键名是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Config>()
                .eq(Config::getConfigKey, config.getConfigKey())
                .ne(Config::getId, config.getId())
        );
        if (count > 0) {
            throw new BusinessException("配置键名已存在");
        }
        
        baseMapper.updateById(config);
        
        // 如果configKey变更，需要清理旧key的缓存
        if (oldConfig.getConfigKey() != null && !oldConfig.getConfigKey().equals(config.getConfigKey())) {
            configCache.remove(oldConfig.getConfigKey());
        }
        
        // 更新新key的缓存
        if (config.getConfigKey() != null && config.getConfigValue() != null) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        Config config = baseMapper.selectById(id);
        if (config != null) {
            if ("Y".equals(config.getConfigType())) {
                throw new BusinessException("系统内置配置不能删除");
            }
            baseMapper.deleteById(id);
            // 清除缓存
            configCache.remove(config.getConfigKey());
        }
    }
    
    @Override
    public void refreshCache() {
        // 清空缓存
        configCache.clear();
        // 重新加载所有配置
        List<Config> configs = baseMapper.selectList(
            new LambdaQueryWrapper<Config>().eq(Config::getStatus, 1)
        );
        for (Config config : configs) {
            configCache.put(config.getConfigKey(), config.getConfigValue());
        }
    }
}
