package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("training_category")
public class Category extends BaseEntity {
    
    /** 分类名称 */
    private String categoryName;
    
    /** 分类编码 */
    private String categoryCode;
    
    /** 分类类型(1课程分类 2题目分类) */
    private Integer categoryType;
    
    /** 父分类ID */
    private Long parentId;
    
    /** 层级 */
    private Integer level;
    
    /** 排序顺序 */
    private Integer sortOrder;
    
    /** 图标 */
    private String icon;
    
    /** 状态(0禁用 1正常) */
    private Integer status;
    
    @TableField(exist = false)
    private String parentName;
    
    @TableField(exist = false)
    private java.util.List<Category> children;
}
