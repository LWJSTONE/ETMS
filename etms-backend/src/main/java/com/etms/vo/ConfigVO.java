package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置响应VO
 */
@Data
public class ConfigVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 配置ID */
    private Long id;
    
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
    
    /** 排序顺序 */
    private Integer sortOrder;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
