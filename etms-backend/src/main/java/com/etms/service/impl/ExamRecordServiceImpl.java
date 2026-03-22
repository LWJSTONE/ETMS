package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.UserMapper;
import com.etms.service.ExamRecordService;
import com.etms.service.UserService;
import com.etms.vo.ExamRecordVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试记录服务实现类
 */
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {
    
    private final PaperMapper paperMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    
    @Override
    public Page<ExamRecordVO> pageExamRecords(Page<ExamRecord> page, Long paperId, Long userId, Integer status) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(status != null, ExamRecord::getStatus, status)
               .orderByDesc(ExamRecord::getCreateTime);
        
        Page<ExamRecord> recordPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ExamRecordVO> voPage = new Page<>();
        BeanUtils.copyProperties(recordPage, voPage, "records");
        
        // 批量获取试卷和用户信息
        List<Long> paperIds = recordPage.getRecords().stream()
                .map(ExamRecord::getPaperId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> userIds = recordPage.getRecords().stream()
                .map(ExamRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Paper> paperMap = paperIds.isEmpty() ? Map.of() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(Collectors.toMap(Paper::getId, p -> p));
        
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        
        List<ExamRecordVO> voList = recordPage.getRecords().stream().map(record -> {
            ExamRecordVO vo = new ExamRecordVO();
            BeanUtils.copyProperties(record, vo);
            
            Paper paper = paperMap.get(record.getPaperId());
            if (paper != null) {
                vo.setPaperName(paper.getPaperName());
                vo.setTotalScore(paper.getTotalScore());
            }
            
            User user = userMap.get(record.getUserId());
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
    public ExamRecordVO getExamRecordDetail(Long id) {
        ExamRecord record = baseMapper.selectById(id);
        if (record == null) {
            return null;
        }
        
        ExamRecordVO vo = new ExamRecordVO();
        BeanUtils.copyProperties(record, vo);
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper != null) {
            vo.setPaperName(paper.getPaperName());
            vo.setTotalScore(paper.getTotalScore());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord startExam(Long paperId, Long planId) {
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查试卷是否存在
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷状态
        if (paper.getStatus() != 1) {
            throw new BusinessException("试卷未发布，无法开始考试");
        }
        
        // 检查是否已有进行中的考试
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUser.getId())
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStatus, 1)
        );
        if (count > 0) {
            throw new BusinessException("您已有进行中的考试，请先完成");
        }
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(currentUser.getId());
        record.setPaperId(paperId);
        record.setPlanId(planId);
        record.setStatus(1); // 进行中
        record.setTotalScore(paper.getTotalScore());
        record.setStartTime(LocalDateTime.now());
        
        baseMapper.insert(record);
        
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Long recordId, String answers) {
        ExamRecord record = baseMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        // 验证考试状态
        if (record.getStatus() != 1) {
            throw new BusinessException("考试已结束");
        }
        
        // 验证是否为当前用户的记录
        User currentUser = userService.getCurrentUser();
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权提交此考试");
        }
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 计算分数（简化处理，实际应根据答案计算）
        // TODO: 实现详细的评分逻辑
        int userScore = calculateScore(record.getPaperId(), answers);
        
        // 更新考试记录
        record.setStatus(2); // 已完成
        record.setUserScore(userScore);
        record.setPassed(userScore >= paper.getPassScore() ? 1 : 0);
        record.setSubmitTime(LocalDateTime.now());
        
        baseMapper.updateById(record);
    }
    
    @Override
    public Page<ExamRecordVO> pageMyExamRecords(Page<ExamRecord> page, Integer status) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        return pageExamRecords(page, null, currentUser.getId(), status);
    }
    
    /**
     * 计算考试分数（简化实现）
     */
    private int calculateScore(Long paperId, String answers) {
        // TODO: 实现详细的评分逻辑
        // 这里简化处理，返回一个默认分数
        return 0;
    }
}
