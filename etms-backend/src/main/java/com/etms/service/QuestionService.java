package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Question;
import com.etms.vo.QuestionVO;
import java.util.List;

/**
 * 题库服务接口
 */
public interface QuestionService extends IService<Question> {
    
    /**
     * 分页查询题目列表
     */
    Page<QuestionVO> pageQuestions(Page<Question> page, String questionContent, Integer questionType, Integer difficulty, Long courseId, Integer status);
    
    /**
     * 获取题目详情
     */
    QuestionVO getQuestionDetail(Long id);
    
    /**
     * 新增题目
     */
    boolean addQuestion(Question question);
    
    /**
     * 更新题目
     */
    boolean updateQuestion(Question question);
    
    /**
     * 删除题目
     */
    boolean deleteQuestion(Long id);
    
    /**
     * 检查题目是否被试卷引用
     * @param questionId 题目ID
     * @return 是否被试卷引用
     */
    boolean isUsedInPapers(Long questionId);
    
    /**
     * 随机抽取题目
     */
    List<QuestionVO> randomQuestions(Integer questionType, Integer difficulty, Integer count, Long courseId);
}
