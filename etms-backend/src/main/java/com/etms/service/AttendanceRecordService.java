package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.AttendanceRecord;
import com.etms.vo.AttendanceRecordVO;

/**
 * 签到记录服务接口
 */
public interface AttendanceRecordService extends IService<AttendanceRecord> {
    
    /**
     * 分页查询签到记录
     */
    Page<AttendanceRecordVO> pageRecords(Page<AttendanceRecord> page, Long planId, Long userId, Integer status);
    
    /**
     * 签到
     */
    boolean signIn(Long planId, Integer signType, String location);
    
    /**
     * 补签审核
     */
    boolean auditSupplement(Long id, Integer auditStatus, String auditRemark);
    
    /**
     * 获取个人签到统计
     */
    AttendanceStatsVO getPersonalStats(Long userId);
}

/**
 * 签到统计VO
 */
class AttendanceStatsVO {
    private Integer totalCount;
    private Integer normalCount;
    private Integer lateCount;
    private Integer earlyCount;
    private Integer absentCount;
    private Double attendanceRate;
    
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getNormalCount() { return normalCount; }
    public void setNormalCount(Integer normalCount) { this.normalCount = normalCount; }
    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }
    public Integer getEarlyCount() { return earlyCount; }
    public void setEarlyCount(Integer earlyCount) { this.earlyCount = earlyCount; }
    public Integer getAbsentCount() { return absentCount; }
    public void setAbsentCount(Integer absentCount) { this.absentCount = absentCount; }
    public Double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(Double attendanceRate) { this.attendanceRate = attendanceRate; }
}
