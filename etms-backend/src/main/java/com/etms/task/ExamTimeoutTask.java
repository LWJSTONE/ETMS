package com.etms.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试超时自动处理定时任务
 * 修复问题：超时未提交的考试记录缺少自动处理机制
 * 
 * 功能说明：
 * 1. 定时扫描进行中(状态0)的考试记录
 * 2. 检查是否超过试卷规定的考试时长
 * 3. 自动将超时考试标记为已超时(状态2)，并计算已提交答案的分数
 * 
 * 状态定义：0-考试中 1-已提交 2-超时 3-已批阅 4-已放弃
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ExamTimeoutTask {
    
    private final ExamRecordMapper examRecordMapper;
    private final PaperMapper paperMapper;
    private final ExamRecordService examRecordService;
    
    /**
     * 自动处理超时考试记录
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoProcessTimeoutExams() {
        log.info("开始执行考试超时自动处理任务...");
        
        int processedCount = 0;
        int errorCount = 0;
        
        try {
            // 1. 查询所有进行中的考试记录
            // 修复：状态0表示考试中，状态1表示已提交，之前错误地使用了状态1
            List<ExamRecord> inProgressRecords = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getStatus, 0) // 状态0表示考试中
                    .isNotNull(ExamRecord::getStartTime) // 有开始时间
                    .isNotNull(ExamRecord::getPaperId) // 有试卷ID
            );
            
            if (inProgressRecords.isEmpty()) {
                log.info("没有进行中的考试记录，跳过处理");
                return;
            }
            
            log.info("发现 {} 条进行中的考试记录，开始检查超时情况", inProgressRecords.size());
            
            LocalDateTime now = LocalDateTime.now();
            
            for (ExamRecord record : inProgressRecords) {
                try {
                    // 2. 获取试卷信息
                    Paper paper = paperMapper.selectById(record.getPaperId());
                    if (paper == null || paper.getExamDuration() == null) {
                        log.warn("考试记录[{}]关联的试卷不存在或未设置考试时长，跳过处理", record.getId());
                        continue;
                    }
                    
                    // 3. 计算已用时间（分钟）
                    long minutesUsed = Duration.between(record.getStartTime(), now).toMinutes();
                    
                    // 4. 检查是否超时（允许1分钟的缓冲时间）
                    if (minutesUsed > paper.getExamDuration() + 1) {
                        // 5. 使用乐观锁更新状态，避免与用户提交操作冲突
                        // 修复：状态定义 0-考试中 1-已提交 2-超时 3-已批阅 4-已放弃
                        int updateCount = examRecordMapper.updateStatusToSubmitted(
                            record.getId(), 0, 2 // 从考试中(0)更新为超时(2)
                        );
                        
                        if (updateCount > 0) {
                            // 6. 计算分数（如果有答案）
                            processTimeoutRecord(record, paper, minutesUsed);
                            processedCount++;
                            log.info("考试记录[{}]已超时自动提交，用时{}分钟，超时{}分钟", 
                                record.getId(), minutesUsed, minutesUsed - paper.getExamDuration());
                        } else {
                            log.debug("考试记录[{}]已被其他线程处理，跳过", record.getId());
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("处理考试记录[{}]超时失败: {}", record.getId(), e.getMessage(), e);
                }
            }
            
            log.info("考试超时自动处理任务完成，处理 {} 条记录，失败 {} 条", processedCount, errorCount);
            
        } catch (Exception e) {
            log.error("考试超时自动处理任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理超时的考试记录
     * @param record 考试记录
     * @param paper 试卷信息
     * @param minutesUsed 已用时间（分钟）
     */
    private void processTimeoutRecord(ExamRecord record, Paper paper, long minutesUsed) {
        // 更新考试记录的其他信息
        ExamRecord updateRecord = new ExamRecord();
        updateRecord.setId(record.getId());
        updateRecord.setDurationUsed((int) minutesUsed);
        updateRecord.setSubmitTime(LocalDateTime.now());
        
        // 如果有答案，计算分数
        if (record.getAnswerDetail() != null && !record.getAnswerDetail().isEmpty()) {
            try {
                // 使用服务层方法计算分数
                int score = calculateTimeoutScore(record.getPaperId(), record.getAnswerDetail());
                updateRecord.setUserScore(score);
                updateRecord.setPassed(score >= paper.getPassScore() ? 1 : 0);
                log.info("超时考试[{}]计算得分: {}, 是否通过: {}", 
                    record.getId(), score, updateRecord.getPassed());
            } catch (Exception e) {
                log.error("超时考试[{}]分数计算失败: {}", record.getId(), e.getMessage());
                updateRecord.setUserScore(0);
                updateRecord.setPassed(0);
            }
        } else {
            // 没有答案，记0分
            updateRecord.setUserScore(0);
            updateRecord.setPassed(0);
            log.info("超时考试[{}]无答案记录，记0分", record.getId());
        }
        
        examRecordMapper.updateById(updateRecord);
    }
    
    /**
     * 计算超时考试的分数
     * @param paperId 试卷ID
     * @param answers 答案JSON
     * @return 得分
     */
    private int calculateTimeoutScore(Long paperId, String answers) {
        try {
            return examRecordService.calculateScore(paperId, answers);
        } catch (Exception e) {
            log.error("计算超时考试分数失败: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 手动触发超时检查（供管理员调用）
     * @return 处理的记录数
     */
    public int manualCheckTimeout() {
        log.info("手动触发考试超时检查");
        autoProcessTimeoutExams();
        return 0;
    }
}
