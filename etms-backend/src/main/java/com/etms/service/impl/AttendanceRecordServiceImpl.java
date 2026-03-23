package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.AttendanceRecord;
import com.etms.entity.TrainingPlan;
import com.etms.entity.User;
import com.etms.entity.UserPlan;
import com.etms.exception.BusinessException;
import com.etms.mapper.AttendanceRecordMapper;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.UserMapper;
import com.etms.mapper.UserPlanMapper;
import com.etms.service.AttendanceRecordService;
import com.etms.vo.AttendanceRecordVO;
import com.etms.vo.AttendanceStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements AttendanceRecordService {
    
    private final UserMapper userMapper;
    private final UserPlanMapper userPlanMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    
    @Override
    public Page<AttendanceRecordVO> pageRecords(Page<AttendanceRecord> page, Long planId, Long userId, Integer status, Integer auditStatus) {
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(planId != null, AttendanceRecord::getPlanId, planId)
               .eq(userId != null, AttendanceRecord::getUserId, userId)
               .eq(status != null, AttendanceRecord::getStatus, status)
               .eq(auditStatus != null, AttendanceRecord::getAuditStatus, auditStatus)
               .orderByDesc(AttendanceRecord::getSignTime);
        
        Page<AttendanceRecord> recordPage = baseMapper.selectPage(page, wrapper);
        
        Page<AttendanceRecordVO> voPage = new Page<>();
        BeanUtils.copyProperties(recordPage, voPage, "records");
        
        // 修复N+1查询问题：批量查询用户信息
        List<Long> userIds = recordPage.getRecords().stream()
                .map(AttendanceRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }
        
        final Map<Long, User> finalUserMap = userMap;
        List<AttendanceRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            AttendanceRecordVO vo = new AttendanceRecordVO();
            BeanUtils.copyProperties(record, vo);
            vo.setSignTypeName(getSignTypeName(record.getSignType()));
            vo.setSignCategoryName(getSignCategoryName(record.getSignCategory()));
            vo.setStatusName(getStatusName(record.getStatus()));
            vo.setReason(record.getReason());
            vo.setAuditRemark(record.getAuditRemark());
            
            // 从批量查询结果中获取用户信息
            User user = finalUserMap.get(record.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
                vo.setRealName(user.getRealName());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean signIn(Long planId, Integer signType, Integer signCategory, String location, String ipAddress, String deviceInfo) {
        Long currentUserId = getCurrentUserId();
        // 添加空指针处理
        if (currentUserId == null) {
            throw new BusinessException("用户未登录，无法签到");
        }
        
        // 修复问题1：校验培训计划是否存在及状态
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("培训计划不存在");
        }
        // 校验培训计划状态：只有已发布(1)或进行中(2)状态可以签到
        if (plan.getStatus() != 1 && plan.getStatus() != 2) {
            throw new BusinessException("培训计划未发布或已结束，无法签到");
        }
        
        // 修复问题3：GPS签到时验证location参数
        if (signType != null && signType == 2) {
            if (location == null || location.trim().isEmpty()) {
                throw new BusinessException("GPS签到必须提供位置信息");
            }
        }
        
        // 校验用户是否属于该培训计划
        Long userPlanCount = userPlanMapper.selectCount(
            new LambdaQueryWrapper<UserPlan>()
                .eq(UserPlan::getUserId, currentUserId)
                .eq(UserPlan::getPlanId, planId)
        );
        if (userPlanCount == null || userPlanCount == 0) {
            throw new BusinessException("您不属于该培训计划，无法签到");
        }
        
        // 检查是否已签到（同一培训计划当天同一签到类别不能重复）
        // 修复：使用signTime字段判断重复签到，而不是createTime
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);
        
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, currentUserId)
                .eq(AttendanceRecord::getPlanId, planId)
                .eq(signCategory != null, AttendanceRecord::getSignCategory, signCategory)
                .between(AttendanceRecord::getSignTime, today, tomorrow)
                .ne(AttendanceRecord::getStatus, 4) // 排除缺勤状态
        );
        if (count > 0) {
            throw new BusinessException("您今天已" + (signCategory != null && signCategory == 2 ? "签退" : "签到") + "，请勿重复操作");
        }
        
        AttendanceRecord record = new AttendanceRecord();
        record.setPlanId(planId);
        record.setSignType(signType);
        record.setSignCategory(signCategory != null ? signCategory : 1); // 默认签到
        record.setLocation(location);
        record.setSignTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUserId(currentUserId);
        
        // 修复问题5：记录IP地址和设备信息
        record.setIpAddress(ipAddress);
        record.setDeviceInfo(deviceInfo);
        
        // 修复问题4：根据培训计划时间自动判断迟到/早退状态，并记录分钟数
        LocalDateTime now = LocalDateTime.now();
        int status = 1; // 默认正常
        if (signCategory == null || signCategory == 1) {
            // 签到时判断是否迟到
            LocalDateTime signStartTime = plan.getSignStartTime();
            if (signStartTime != null && now.isAfter(signStartTime)) {
                status = 2; // 迟到
                // 计算迟到分钟数
                long lateMins = java.time.Duration.between(signStartTime, now).toMinutes();
                record.setLateMinutes((int) lateMins);
            }
        } else if (signCategory == 2) {
            // 签退时判断是否早退
            LocalDateTime signEndTime = plan.getSignEndTime();
            if (signEndTime != null && now.isBefore(signEndTime)) {
                status = 3; // 早退
                // 计算早退分钟数
                long earlyMins = java.time.Duration.between(now, signEndTime).toMinutes();
                record.setEarlyMinutes((int) earlyMins);
            }
        }
        record.setStatus(status);
        
        return baseMapper.insert(record) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applySupplementary(Long planId, Integer signType, Integer signCategory, String signTime, String reason) {
        Long currentUserId = getCurrentUserId();
        // 添加空指针处理
        if (currentUserId == null) {
            throw new BusinessException("用户未登录，无法申请补签");
        }
        
        // 校验培训计划是否存在
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验用户是否属于该培训计划（与签到方法保持一致）
        Long userPlanCount = userPlanMapper.selectCount(
            new LambdaQueryWrapper<UserPlan>()
                .eq(UserPlan::getUserId, currentUserId)
                .eq(UserPlan::getPlanId, planId)
        );
        if (userPlanCount == null || userPlanCount == 0) {
            throw new BusinessException("您不属于该培训计划，无法申请补签");
        }
        
        // 解析并校验补签时间
        LocalDateTime parsedSignTime;
        if (signTime != null && !signTime.isEmpty()) {
            try {
                parsedSignTime = LocalDateTime.parse(signTime.replace(" ", "T"));
                // 校验补签时间不能晚于当前时间
                if (parsedSignTime.isAfter(LocalDateTime.now())) {
                    throw new BusinessException("补签时间不能晚于当前时间");
                }
                // 校验补签时间不能超过30天
                if (parsedSignTime.isBefore(LocalDateTime.now().minusDays(30))) {
                    throw new BusinessException("只能补签30天内的记录");
                }
                // 修复问题6：验证补签时间是否在培训计划有效期内
                LocalDate signDate = parsedSignTime.toLocalDate();
                if (plan.getStartDate() != null && signDate.isBefore(plan.getStartDate())) {
                    throw new BusinessException("补签时间不能早于培训计划开始日期(" + plan.getStartDate() + ")");
                }
                if (plan.getEndDate() != null && signDate.isAfter(plan.getEndDate())) {
                    throw new BusinessException("补签时间不能晚于培训计划结束日期(" + plan.getEndDate() + ")");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                parsedSignTime = LocalDateTime.now();
            }
        } else {
            parsedSignTime = LocalDateTime.now();
        }
        
        // 检查是否已有该时间段的签到记录
        LocalDateTime dayStart = parsedSignTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime dayEnd = dayStart.plusDays(1);
        Long existCount = baseMapper.selectCount(
            new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, currentUserId)
                .eq(AttendanceRecord::getPlanId, planId)
                .eq(signCategory != null, AttendanceRecord::getSignCategory, signCategory)
                .between(AttendanceRecord::getSignTime, dayStart, dayEnd)
                .ne(AttendanceRecord::getStatus, 4) // 排除缺勤状态
        );
        if (existCount > 0) {
            throw new BusinessException("该时间段已有签到记录，无法重复补签");
        }
        
        AttendanceRecord record = new AttendanceRecord();
        record.setPlanId(planId);
        record.setSignType(signType);
        record.setSignCategory(signCategory != null ? signCategory : 1); // 默认签到
        record.setReason(reason);
        record.setStatus(5); // 补签
        record.setAuditStatus(0); // 待审核
        record.setCreateTime(LocalDateTime.now());
        record.setUserId(currentUserId);
        record.setSignTime(parsedSignTime);
        
        return baseMapper.insert(record) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelSupplementary(Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 只能撤销待审核的补签申请
        AttendanceRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("补签记录不存在");
        }
        
        // 验证是否为申请人本人
        if (!record.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能撤销自己的补签申请");
        }
        
        if (record.getStatus() != 5 || record.getAuditStatus() != 0) {
            throw new BusinessException("只能撤销待审核的补签申请");
        }
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditSupplement(Long id, Integer auditStatus, String auditRemark) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 获取原记录
        AttendanceRecord existingRecord = baseMapper.selectById(id);
        if (existingRecord == null) {
            throw new BusinessException("记录不存在");
        }
        
        // 验证是否为补签记录
        if (existingRecord.getStatus() != 5) {
            throw new BusinessException("只能审核补签记录");
        }
        
        // 验证是否已审核
        if (existingRecord.getAuditStatus() != 0) {
            throw new BusinessException("该记录已审核，不能重复审核");
        }
        
        AttendanceRecord record = new AttendanceRecord();
        record.setId(id);
        record.setAuditStatus(auditStatus);
        record.setAuditRemark(auditRemark);
        record.setAuditBy(currentUserId);
        record.setAuditTime(LocalDateTime.now());
        
        // 审核通过时更新状态为正常签到
        if (auditStatus == 1) {
            record.setStatus(1); // 更新为正常状态
        } else if (auditStatus == 2) {
            // 审核驳回时更新状态为补签驳回(status=6)
            record.setStatus(6);
        }
        
        return baseMapper.updateById(record) > 0;
    }
    
    @Override
    public AttendanceStatsVO getPersonalStats(Long userId) {
        AttendanceStatsVO stats = new AttendanceStatsVO();
        
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRecord::getUserId, userId);
        
        long total = baseMapper.selectCount(wrapper);
        stats.setTotalCount((int) total);
        
        // 修复：正常签到数量应包含审核通过的补签记录（status=1 或者 status=5且auditStatus=1）
        LambdaQueryWrapper<AttendanceRecord> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(AttendanceRecord::getUserId, userId)
                     .and(w -> w.eq(AttendanceRecord::getStatus, 1)
                         .or(sub -> sub.eq(AttendanceRecord::getStatus, 5)
                             .eq(AttendanceRecord::getAuditStatus, 1)));
        long normal = baseMapper.selectCount(normalWrapper);
        stats.setNormalCount((int) normal);
        
        // 待审核数量
        LambdaQueryWrapper<AttendanceRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(AttendanceRecord::getUserId, userId)
                      .eq(AttendanceRecord::getStatus, 5)
                      .eq(AttendanceRecord::getAuditStatus, 0);
        long pending = baseMapper.selectCount(pendingWrapper);
        stats.setPendingCount((int) pending);
        
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
    
    private String getSignCategoryName(Integer signCategory) {
        if (signCategory == null) return "未知";
        switch (signCategory) {
            case 1: return "签到";
            case 2: return "签退";
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
            case 6: return "补签驳回";
            default: return "未知";
        }
    }
    
    /**
     * 根据培训计划时间计算考勤状态
     * @param planId 培训计划ID
     * @param signCategory 签到类别(1签到 2签退)
     * @return 状态(1正常 2迟到 3早退)
     */
    private int calculateAttendanceStatus(Long planId, Integer signCategory) {
        // 查询培训计划
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            return 1; // 默认正常
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 签到时判断是否迟到
        if (signCategory == null || signCategory == 1) {
            LocalDateTime signStartTime = plan.getSignStartTime();
            if (signStartTime != null && now.isAfter(signStartTime)) {
                return 2; // 迟到
            }
        }
        
        // 签退时判断是否早退
        if (signCategory != null && signCategory == 2) {
            LocalDateTime signEndTime = plan.getSignEndTime();
            if (signEndTime != null && now.isBefore(signEndTime)) {
                return 3; // 早退
            }
        }
        
        return 1; // 正常
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 从认证信息中获取用户名，然后查询用户ID
        String username = authentication.getName();
        User user = userMapper.selectByUsername(username);
        return user != null ? user.getId() : null;
    }
}
