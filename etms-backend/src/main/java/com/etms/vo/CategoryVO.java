package com.etms.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类视图对象
 */
@Data
public class CategoryVO {
    
    private Long id;
    
    private String categoryName;
    
    private String categoryCode;
    
    private Integer categoryType;
    
    private Long parentId;
    
    private String parentName;
    
    private Integer level;
    
    private Integer sortOrder;
    
    private String icon;
    
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private List<CategoryVO> children;
}
