package com.etms.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 签到统计响应VO
 */
@Data
public class AttendanceStatsVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 总签到次数 */
    private Integer totalCount;
    
    /** 正常签到次数 */
    private Integer normalCount;
    
    /** 待审核数量 */
    private Integer pendingCount;
    
    /** 出勤率(百分比) */
    private Double attendanceRate;
}
