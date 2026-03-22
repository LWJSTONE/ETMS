package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Config;

/**
 * 系统配置服务接口
 */
public interface ConfigService extends IService<Config> {
    
    /**
     * 分页查询配置列表
     */
    Page<Config> pageConfigs(Page<Config> page, String configName, String configKey, Integer status);
    
    /**
     * 获取配置详情
     */
    Config getConfigDetail(Long id);
    
    /**
     * 根据配置键名获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 新增配置
     */
    void addConfig(Config config);
    
    /**
     * 更新配置
     */
    void updateConfig(Config config);
    
    /**
     * 删除配置
     */
    void deleteConfig(Long id);
    
    /**
     * 刷新缓存
     */
    void refreshCache();
}
