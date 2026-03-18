package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 */
@Data
public class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    
    /** 备注 */
    private String remark;
}
