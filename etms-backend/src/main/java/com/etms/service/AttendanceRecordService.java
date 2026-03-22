package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.AttendanceRecord;
import com.etms.vo.AttendanceRecordVO;
import com.etms.vo.AttendanceStatsVO;

/**
 * 签到记录服务接口
 */
public interface AttendanceRecordService extends IService<AttendanceRecord> {
    
    /**
     * 分页查询签到记录
     */
    Page<AttendanceRecordVO> pageRecords(Page<AttendanceRecord> page, Long planId, Long userId, Integer status, Integer auditStatus);
    
    /**
     * 签到/签退
     */
    boolean signIn(Long planId, Integer signType, Integer signCategory, String location);
    
    /**
     * 补签申请
     */
    boolean applySupplementary(Long planId, Integer signType, Integer signCategory, String signTime, String reason);
    
    /**
     * 撤销补签申请
     */
    boolean cancelSupplementary(Long id);
    
    /**
     * 补签审核
     */
    boolean auditSupplement(Long id, Integer auditStatus, String auditRemark);
    
    /**
     * 获取个人签到统计
     */
    AttendanceStatsVO getPersonalStats(Long userId);
}
