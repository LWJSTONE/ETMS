package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class DictType extends BaseEntity {
    
    /** 字典名称 */
    private String dictName;
    
    /** 字典类型 */
    private String dictType;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
    
    /** 备注 */
    private String remark;
}
