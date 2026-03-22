package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Question;
import com.etms.mapper.QuestionMapper;
import com.etms.service.QuestionService;
import com.etms.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库服务实现类
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
    @Override
    public Page<QuestionVO> pageQuestions(Page<Question> page, String questionContent, Integer questionType, Integer difficulty, Long courseId, Integer status) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(questionContent), Question::getQuestionContent, questionContent)
               .eq(questionType != null, Question::getQuestionType, questionType)
               .eq(difficulty != null, Question::getDifficulty, difficulty)
               .eq(courseId != null, Question::getCourseId, courseId)
               .eq(status != null, Question::getStatus, status)
               .orderByDesc(Question::getCreateTime);
        
        Page<Question> questionPage = baseMapper.selectPage(page, wrapper);
        
        Page<QuestionVO> voPage = new Page<>();
        BeanUtils.copyProperties(questionPage, voPage, "records");
        
        List<QuestionVO> voList = questionPage.getRecords().stream().map(question -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(question, vo);
            vo.setQuestionTypeName(getQuestionTypeName(question.getQuestionType()));
            vo.setDifficultyName(getDifficultyName(question.getDifficulty()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public QuestionVO getQuestionDetail(Long id) {
        Question question = baseMapper.selectById(id);
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        vo.setQuestionTypeName(getQuestionTypeName(question.getQuestionType()));
        vo.setDifficultyName(getDifficultyName(question.getDifficulty()));
        return vo;
    }
    
    @Override
    public boolean addQuestion(Question question) {
        question.setStatus(1);
        return baseMapper.insert(question) > 0;
    }
    
    @Override
    public boolean updateQuestion(Question question) {
        return baseMapper.updateById(question) > 0;
    }
    
    @Override
    public boolean deleteQuestion(Long id) {
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<QuestionVO> randomQuestions(Integer questionType, Integer difficulty, Integer count, Long courseId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(questionType != null, Question::getQuestionType, questionType)
               .eq(difficulty != null, Question::getDifficulty, difficulty)
               .eq(courseId != null, Question::getCourseId, courseId)
               .eq(Question::getStatus, 1)
               .last("ORDER BY RAND() LIMIT " + count);
        
        List<Question> questions = baseMapper.selectList(wrapper);
        
        return questions.stream().map(question -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(question, vo);
            vo.setQuestionTypeName(getQuestionTypeName(question.getQuestionType()));
            vo.setDifficultyName(getDifficultyName(question.getDifficulty()));
            return vo;
        }).collect(Collectors.toList());
    }
    
    private String getQuestionTypeName(Integer questionType) {
        if (questionType == null) return "未知";
        switch (questionType) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            default: return "未知";
        }
    }
    
    private String getDifficultyName(Integer difficulty) {
        if (difficulty == null) return "未知";
        switch (difficulty) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "未知";
        }
    }
}
