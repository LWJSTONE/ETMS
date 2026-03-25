package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.exception.BusinessException;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.service.QuestionService;
import com.etms.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题库服务实现类
 */
@Slf4j
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
        // 验证必填字段
        if (question.getQuestionContent() == null || question.getQuestionContent().trim().isEmpty()) {
            throw new BusinessException("题目内容不能为空");
        }
        if (question.getQuestionType() == null) {
            throw new BusinessException("题目类型不能为空");
        }
        
        // 选择题和判断题必须有答案
        if (question.getQuestionType() == 1 || question.getQuestionType() == 2 || question.getQuestionType() == 3) {
            if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                throw new BusinessException("选择题和判断题必须设置答案");
            }
        }
        
        // 修复：填空题(类型4)必须有答案
        if (question.getQuestionType() == 4) {
            if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                throw new BusinessException("填空题必须设置答案");
            }
        }
        
        // 修复：简答题(类型5)建议设置参考答案（可选，但给出提示）
        if (question.getQuestionType() == 5) {
            if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                // 简答题可以不设置答案，但记录日志提示
                log.info("简答题未设置参考答案，后续需要人工评分");
            }
        }
        
        // 验证选项依赖关系
        validateOptions(question);
        
        question.setStatus(1);
        return baseMapper.insert(question) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuestion(Question question) {
        // 检查题目是否被已发布的试卷使用
        // 修复：如果题目被已发布试卷使用，禁止修改关键内容
        Long usedInPublishedPapers = baseMapper.countUsedInPublishedPapers(question.getId());
        if (usedInPublishedPapers != null && usedInPublishedPapers > 0) {
            throw new BusinessException("该题目已被已发布的试卷使用，无法修改。请创建新题目或联系管理员。");
        }
        
        // 检查题目是否被试卷引用（未发布的试卷）
        Long usedInExamCount = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getQuestionId, question.getId())
        );
        if (usedInExamCount > 0) {
            // 题目已被试卷引用（未发布），记录日志但不禁止修改
            log.warn("题目ID={} 已被 {} 个试卷引用，修改可能影响这些试卷", question.getId(), usedInExamCount);
        }
        
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
            
            // 至少需要两个选项（A和B）
            if (!hasContent(optionA) || !hasContent(optionB)) {
                throw new BusinessException("选择题至少需要填写A、B两个选项");
            }
            
            // 如果填写了C选项，必须先有A、B选项
            if (hasContent(optionC)) {
                if (!hasContent(optionA) || !hasContent(optionB)) {
                    throw new BusinessException("填写C选项前，必须先填写A、B选项");
                }
            }
            
            // 如果填写了D选项，必须先有C选项
            if (hasContent(optionD)) {
                if (!hasContent(optionC)) {
                    throw new BusinessException("填写D选项前，必须先填写C选项");
                }
            }
            
            // 如果填写了E选项，必须先有D选项
            if (hasContent(optionE)) {
                if (!hasContent(optionD)) {
                    throw new BusinessException("填写E选项前，必须先填写D选项");
                }
            }
            
            // 验证答案必须在有效选项范围内
            validateAnswer(question);
        }
        
        // 判断题验证
        if (question.getQuestionType() != null && question.getQuestionType() == 3) {
            String answer = question.getAnswer();
            if (answer != null && !answer.trim().isEmpty()) {
                String normalizedAnswer = normalizeJudgeAnswer(answer);
                if (!normalizedAnswer.equals("A") && !normalizedAnswer.equals("B")) {
                    throw new BusinessException("判断题答案必须是A(正确)或B(错误)");
                }
            }
        }
    }
    
    /**
     * 验证答案是否在有效选项范围内
     */
    private void validateAnswer(Question question) {
        String answer = question.getAnswer();
        if (answer == null || answer.trim().isEmpty()) {
            return; // 答案可以为空，允许后续补充
        }
        
        String normalizedAnswer = answer.toUpperCase().trim();
        
        // 单选题答案验证
        if (question.getQuestionType() == 1) {
            if (!isValidOption(normalizedAnswer, question)) {
                throw new BusinessException("单选题答案必须是有效选项(A-E)");
            }
        }
        
        // 多选题答案验证
        if (question.getQuestionType() == 2) {
            String[] answers = normalizedAnswer.split("[,，]\\s*");
            for (String a : answers) {
                if (!isValidOption(a.trim(), question)) {
                    throw new BusinessException("多选题答案包含无效选项: " + a);
                }
            }
        }
    }
    
    /**
     * 检查选项是否有效
     */
    private boolean isValidOption(String option, Question question) {
        if (option == null || option.length() != 1) {
            return false;
        }
        char c = option.charAt(0);
        if (c < 'A' || c > 'E') {
            return false;
        }
        // 检查对应选项是否存在
        switch (c) {
            case 'A': return hasContent(question.getOptionA());
            case 'B': return hasContent(question.getOptionB());
            case 'C': return hasContent(question.getOptionC());
            case 'D': return hasContent(question.getOptionD());
            case 'E': return hasContent(question.getOptionE());
            default: return false;
        }
    }
    
    /**
     * 标准化判断题答案
     */
    private String normalizeJudgeAnswer(String answer) {
        if (answer == null) return "";
        answer = answer.trim();
        // 将"正确"、"对"、"T"、"True"转换为"A"
        if (answer.equals("正确") || answer.equals("对") || answer.equalsIgnoreCase("T") 
            || answer.equalsIgnoreCase("True") || answer.equals("A")) {
            return "A";
        }
        // 将"错误"、"错"、"F"、"False"转换为"B"
        if (answer.equals("错误") || answer.equals("错") || answer.equalsIgnoreCase("F") 
            || answer.equalsIgnoreCase("False") || answer.equals("B")) {
            return "B";
        }
        return answer;
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
            throw new BusinessException("题目已被试卷引用，无法删除");
        }
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean isUsedInPapers(Long questionId) {
        Long count = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getQuestionId, questionId)
        );
        return count > 0;
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
