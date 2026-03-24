package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Paper;
import com.etms.vo.PaperVO;
import com.etms.vo.PaperQuestionVO;
import java.util.List;
import java.util.Map;

/**
 * 试卷服务接口
 */
public interface PaperService extends IService<Paper> {
    
    /**
     * 分页查询试卷列表
     */
    Page<Paper> pagePapers(Page<Paper> page, String paperName, String paperCode, Integer status);
    
    /**
     * 获取试卷详情（含题目）
     * @param id 试卷ID
     * @return 试卷详情
     */
    PaperVO getPaperDetail(Long id);
    
    /**
     * 获取试卷详情（含题目）
     * @param id 试卷ID
     * @param forExam 是否为考试场景（考试场景下隐藏答案和解析）
     * @return 试卷详情
     */
    PaperVO getPaperDetail(Long id, boolean forExam);
    
    /**
     * 获取试卷详情（含题目）- 考试场景
     * @param id 试卷ID
     * @param forExam 是否为考试场景（考试场景下隐藏答案和解析）
     * @param planId 培训计划ID（用于验证考试资格）
     * @return 试卷详情
     */
    PaperVO getPaperDetail(Long id, boolean forExam, Long planId);
    
    /**
     * 新增试卷
     */
    void addPaper(Paper paper);
    
    /**
     * 更新试卷
     */
    void updatePaper(Paper paper);
    
    /**
     * 删除试卷
     */
    void deletePaper(Long id);
    
    /**
     * 发布试卷
     */
    void publishPaper(Long id);
    
    /**
     * 停用试卷
     */
    void disablePaper(Long id);
    
    /**
     * 检查试卷是否有考试记录
     * @param paperId 试卷ID
     * @return 是否有考试记录
     */
    boolean hasExamRecords(Long paperId);
    
    // ==================== 组卷管理方法 ====================
    
    /**
     * 获取试卷题目列表
     * @param paperId 试卷ID
     * @return 题目列表
     */
    List<PaperQuestionVO> getPaperQuestions(Long paperId);
    
    /**
     * 批量添加题目到试卷
     * @param paperId 试卷ID
     * @param questions 题目列表（包含questionId, score, sortOrder）
     */
    void batchAddQuestions(Long paperId, List<Map<String, Object>> questions);
    
    /**
     * 从试卷移除单个题目
     * @param paperId 试卷ID
     * @param questionId 题目ID
     */
    void removeQuestionFromPaper(Long paperId, Long questionId);
    
    /**
     * 清空试卷所有题目
     * @param paperId 试卷ID
     */
    void clearPaperQuestions(Long paperId);
}
