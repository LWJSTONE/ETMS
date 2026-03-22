package com.etms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 签到记录响应VO
 */
@Data
public class AttendanceRecordVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 签到ID */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 用户名 */
    private String userName;
    
    /** 真实姓名 */
    private String realName;
    
    /** 计划ID */
    private Long planId;
    
    /** 计划名称 */
    private String planName;
    
    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signTime;
    
    /** 签到类型 */
    private Integer signType;
    
    /** 签到类型名称 */
    private String signTypeName;
    
    /** 签到位置 */
    private String location;
    
    /** 状态 */
    private Integer status;
    
    /** 状态名称 */
    private String statusName;
    
    /** 迟到分钟数 */
    private Integer lateMinutes;
    
    /** 早退分钟数 */
    private Integer earlyMinutes;
    
    /** 补签原因 */
    private String reason;
    
    /** 审核状态 */
    private Integer auditStatus;
    
    /** 审核备注 */
    private String auditRemark;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
