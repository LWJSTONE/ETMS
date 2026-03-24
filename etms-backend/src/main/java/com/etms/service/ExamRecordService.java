package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.entity.ExamRecord;
import com.etms.vo.ExamRecordVO;
import com.etms.vo.ExamResultVO;
import com.etms.vo.ExamResultStatsVO;

import java.io.OutputStream;

/**
 * 考试记录服务接口
 */
public interface ExamRecordService {
    
    /**
     * 分页查询考试记录
     */
    Page<ExamRecordVO> pageExamRecords(Page<ExamRecord> page, Long paperId, Long userId, Integer status, String userName, String paperName);
    
    /**
     * 获取考试记录详情
     */
    ExamRecordVO getExamRecordDetail(Long id);
    
    /**
     * 获取考试记录详情（带权限校验）
     * 用户只能查看自己的考试记录，管理员可以查看所有
     */
    ExamRecordVO getExamRecordDetailForUser(Long id);
    
    /**
     * 开始考试
     */
    ExamRecord startExam(Long paperId, Long planId);
    
    /**
     * 提交试卷
     */
    void submitExam(Long recordId, String answers);
    
    /**
     * 放弃考试
     */
    void giveUpExam(Long recordId);
    
    /**
     * 获取当前用户的考试记录
     */
    Page<ExamRecordVO> pageMyExamRecords(Page<ExamRecord> page, Integer status);
    
    /**
     * 分页查询成绩列表
     */
    Page<ExamResultVO> pageResults(Long current, Long size, Long paperId, Long userId, Integer passed, String userName, String paperName, String startTime, String endTime);
    
    /**
     * 获取我的成绩
     */
    Page<ExamResultVO> getMyResults(Long current, Long size, Integer passed, String paperName, String startTime, String endTime);
    
    /**
     * 获取成绩详情
     */
    ExamResultVO getResultDetail(Long id);
    
    /**
     * 获取成绩统计
     */
    ExamResultStatsVO getResultStats(String startTime, String endTime);
    
    /**
     * 导出考试记录
     */
    void exportRecords(Long paperId, Long userId, Integer status, String userName, String paperName, OutputStream outputStream);
    
    /**
     * 导出成绩列表
     */
    void exportResults(Long paperId, Long userId, Integer passed, String userName, String paperName, String startTime, String endTime, OutputStream outputStream);
    
    /**
     * 计算考试分数
     * @param paperId 试卷ID
     * @param answers 用户答案（JSON格式）
     * @return 用户得分
     */
    int calculateScore(Long paperId, String answers);
}
