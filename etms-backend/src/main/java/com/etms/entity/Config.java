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
    
    /** 配置类型(1系统 2业务) */
    private Integer configType;
    
    /** 是否可编辑 */
    private Integer isEditable;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
    
    /** 排序顺序 */
    private Integer sortOrder;
}
