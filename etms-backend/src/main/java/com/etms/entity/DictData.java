package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class DictData extends BaseEntity {
    
    /** 字典类型ID */
    private Long dictTypeId;
    
    /** 数据标签 */
    private String dictLabel;
    
    /** 数据键值 */
    private String dictValue;
    
    /** 排序 */
    private Integer dictSort;
    
    /** 状态(0禁用 1启用) */
    private Integer status;
}
