package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.AttendanceRecord;
import com.etms.mapper.AttendanceRecordMapper;
import com.etms.service.AttendanceRecordService;
import com.etms.vo.AttendanceRecordVO;
import com.etms.vo.AttendanceStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements AttendanceRecordService {
    
    @Override
    public Page<AttendanceRecordVO> pageRecords(Page<AttendanceRecord> page, Long planId, Long userId, Integer status) {
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, AttendanceRecord::getPlanId, planId)
               .eq(userId != null, AttendanceRecord::getUserId, userId)
               .eq(status != null, AttendanceRecord::getStatus, status)
               .orderByDesc(AttendanceRecord::getSignTime);
        
        Page<AttendanceRecord> recordPage = baseMapper.selectPage(page, wrapper);
        
        Page<AttendanceRecordVO> voPage = new Page<>();
        BeanUtils.copyProperties(recordPage, voPage, "records");
        
        List<AttendanceRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            AttendanceRecordVO vo = new AttendanceRecordVO();
            BeanUtils.copyProperties(record, vo);
            vo.setSignTypeName(getSignTypeName(record.getSignType()));
            vo.setStatusName(getStatusName(record.getStatus()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public boolean signIn(Long planId, Integer signType, String location) {
        AttendanceRecord record = new AttendanceRecord();
        record.setPlanId(planId);
        record.setSignType(signType);
        record.setLocation(location);
        record.setSignTime(LocalDateTime.now());
        record.setStatus(1); // 正常
        // record.setUserId(getCurrentUserId());
        
        return baseMapper.insert(record) > 0;
    }
    
    @Override
    public boolean auditSupplement(Long id, Integer auditStatus, String auditRemark) {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(id);
        record.setAuditStatus(auditStatus);
        // record.setAuditBy(getCurrentUserId());
        record.setAuditTime(LocalDateTime.now());
        
        return baseMapper.updateById(record) > 0;
    }
    
    @Override
    public AttendanceStatsVO getPersonalStats(Long userId) {
        AttendanceStatsVO stats = new AttendanceStatsVO();
        
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRecord::getUserId, userId);
        
        long total = baseMapper.selectCount(wrapper);
        stats.setTotalCount((int) total);
        
        wrapper.eq(AttendanceRecord::getStatus, 1);
        long normal = baseMapper.selectCount(wrapper);
        stats.setNormalCount((int) normal);
        
        if (total > 0) {
            stats.setAttendanceRate((double) normal / total * 100);
        } else {
            stats.setAttendanceRate(0.0);
        }
        
        return stats;
    }
    
    private String getSignTypeName(Integer signType) {
        if (signType == null) return "未知";
        switch (signType) {
            case 1: return "二维码";
            case 2: return "GPS定位";
            case 3: return "人脸识别";
            default: return "未知";
        }
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "正常";
            case 2: return "迟到";
            case 3: return "早退";
            case 4: return "缺勤";
            case 5: return "补签";
            default: return "未知";
        }
    }
}
