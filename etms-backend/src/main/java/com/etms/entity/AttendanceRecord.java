package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 签到记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_record")
public class AttendanceRecord extends BaseEntity {
    
    /** 用户ID */
    private Long userId;
    
    /** 计划ID */
    private Long planId;
    
    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signTime;
    
    /** 签到类型(1二维码 2GPS定位 3人脸识别) */
    private Integer signType;
    
    /** 签到类别(1签到 2签退) */
    private Integer signCategory;
    
    /** 签到位置 */
    private String location;
    
    /** 签到IP地址 */
    private String ipAddress;
    
    /** 设备信息 */
    private String deviceInfo;
    
    /** 状态(1正常 2迟到 3早退 4缺勤 5补签) */
    private Integer status;
    
    /** 迟到分钟数 */
    private Integer lateMinutes;
    
    /** 早退分钟数 */
    private Integer earlyMinutes;
    
    /** 补签原因 */
    private String reason;
    
    /** 审核备注 */
    private String auditRemark;
    
    /** 审核状态(0待审核 1通过 2驳回) */
    private Integer auditStatus;
    
    /** 审核人ID */
    private Long auditBy;
    
    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String planName;
    
    /**
     * 重写deleted字段，因为attendance_record表没有deleted字段
     * 标记为不存在于数据库表中，避免MyBatis-Plus自动添加deleted条件
     */
    @TableField(exist = false)
    private Integer deleted;
}
