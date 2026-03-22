package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.entity.ExamRecord;
import com.etms.vo.ExamRecordVO;
import com.etms.vo.ExamResultVO;

/**
 * 考试记录服务接口
 */
public interface ExamRecordService {
    
    /**
     * 分页查询考试记录
     */
    Page<ExamRecordVO> pageExamRecords(Page<ExamRecord> page, Long paperId, Long userId, Integer status);
    
    /**
     * 获取考试记录详情
     */
    ExamRecordVO getExamRecordDetail(Long id);
    
    /**
     * 开始考试
     */
    ExamRecord startExam(Long paperId, Long planId);
    
    /**
     * 提交试卷
     */
    void submitExam(Long recordId, String answers);
    
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
    Page<ExamResultVO> getMyResults(Long current, Long size, Integer passed);
    
    /**
     * 获取成绩详情
     */
    ExamResultVO getResultDetail(Long id);
}
