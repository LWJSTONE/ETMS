package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class Config extends BaseEntity {
    
    /** 配置名称 */
    private String configName;
    
    /** 配置键名 */
    private String configKey;
    
    /** 配置键值 */
    private String configValue;
    
    /** 系统内置(Y是 N否) */
    private String configType;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
    
    /** 备注 */
    private String remark;
}
