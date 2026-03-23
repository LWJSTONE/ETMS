package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.service.QuestionService;
import com.etms.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库服务实现类
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
    private final PaperQuestionMapper paperQuestionMapper;
    
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
    @Transactional(rollbackFor = Exception.class)
    public boolean addQuestion(Question question) {
        // 验证选项依赖关系
        validateOptions(question);
        
        question.setStatus(1);
        return baseMapper.insert(question) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestion(Question question) {
        // 验证选项依赖关系
        validateOptions(question);
        
        return baseMapper.updateById(question) > 0;
    }
    
    /**
     * 验证选项依赖关系
     * 规则：填C需先有A、B，填D需先有C，填E需先有D
     */
    private void validateOptions(Question question) {
        // 只对选择题（单选、多选）进行选项验证
        if (question.getQuestionType() != null && (question.getQuestionType() == 1 || question.getQuestionType() == 2)) {
            String optionA = question.getOptionA();
            String optionB = question.getOptionB();
            String optionC = question.getOptionC();
            String optionD = question.getOptionD();
            String optionE = question.getOptionE();
            
            // 如果填写了C选项，必须先有A、B选项
            if (hasContent(optionC)) {
                if (!hasContent(optionA) || !hasContent(optionB)) {
                    throw new RuntimeException("填写C选项前，必须先填写A、B选项");
                }
            }
            
            // 如果填写了D选项，必须先有C选项
            if (hasContent(optionD)) {
                if (!hasContent(optionC)) {
                    throw new RuntimeException("填写D选项前，必须先填写C选项");
                }
            }
            
            // 如果填写了E选项，必须先有D选项
            if (hasContent(optionE)) {
                if (!hasContent(optionD)) {
                    throw new RuntimeException("填写E选项前，必须先填写D选项");
                }
            }
            
            // 至少需要两个选项（A和B）
            if (!hasContent(optionA) || !hasContent(optionB)) {
                throw new RuntimeException("选择题至少需要填写A、B两个选项");
            }
        }
    }
    
    /**
     * 检查字符串是否有内容
     */
    private boolean hasContent(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteQuestion(Long id) {
        // 检查题目是否被试卷引用
        Long count = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getQuestionId, id)
        );
        if (count > 0) {
            throw new RuntimeException("题目已被试卷引用，无法删除");
        }
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<QuestionVO> randomQuestions(Integer questionType, Integer difficulty, Integer count, Long courseId) {
        // 使用Mapper方法替代字符串拼接，避免SQL注入
        List<Question> questions = baseMapper.selectRandomQuestions(questionType, difficulty, count, courseId);
        
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
