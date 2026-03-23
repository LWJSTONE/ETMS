package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.exception.BusinessException;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.service.PaperService;
import com.etms.vo.PaperQuestionVO;
import com.etms.vo.PaperVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper> implements PaperService {

    private final ExamRecordMapper examRecordMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Page<Paper> pagePapers(Page<Paper> page, String paperName, String paperCode, Integer status) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(paperName), Paper::getPaperName, paperName)
               .like(StringUtils.hasText(paperCode), Paper::getPaperCode, paperCode)
               .eq(status != null, Paper::getStatus, status)
               .orderByDesc(Paper::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Object getPaperDetail(Long id) {
        Paper paper = baseMapper.selectById(id);
        if (paper == null) {
            return null;
        }

        PaperVO vo = new PaperVO();
        BeanUtils.copyProperties(paper, vo);
        
        // 手动设置duration字段（Paper.examDuration -> PaperVO.duration）
        vo.setDuration(paper.getExamDuration());

        // 查询试卷关联的题目
        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
            new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, id)
                .orderByAsc(PaperQuestion::getSortOrder)
        );

        if (!paperQuestions.isEmpty()) {
            // 获取所有题目ID
            List<Long> questionIds = paperQuestions.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());

            // 批量查询题目详情
            List<Question> questions = questionMapper.selectBatchIds(questionIds);
            Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

            // 构建题目分数映射
            Map<Long, Integer> scoreMap = paperQuestions.stream()
                .collect(Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getScore));

            // 构建题目排序映射
            Map<Long, Integer> sortOrderMap = paperQuestions.stream()
                .collect(Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getSortOrder));

            // 转换为VO列表
            List<PaperQuestionVO> questionVOList = new ArrayList<>();
            for (PaperQuestion pq : paperQuestions) {
                Question question = questionMap.get(pq.getQuestionId());
                if (question != null) {
                    PaperQuestionVO qvo = new PaperQuestionVO();
                    BeanUtils.copyProperties(question, qvo);
                    qvo.setQuestionId(question.getId());
                    qvo.setScore(scoreMap.getOrDefault(question.getId(), question.getScore()));
                    qvo.setSortOrder(sortOrderMap.get(question.getId()));
                    questionVOList.add(qvo);
                }
            }
            vo.setQuestions(questionVOList);
        } else {
            vo.setQuestions(new ArrayList<>());
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPaper(Paper paper) {
        // 检查试卷编码是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Paper>().eq(Paper::getPaperCode, paper.getPaperCode())
        );
        if (count > 0) {
            throw new BusinessException("试卷编码已存在");
        }
        
        // 验证分数合理性
        if (paper.getPassScore() != null && paper.getTotalScore() != null) {
            if (paper.getPassScore() > paper.getTotalScore()) {
                throw new BusinessException("及格分不能大于总分");
            }
            if (paper.getPassScore() <= 0) {
                throw new BusinessException("及格分必须大于0");
            }
        }
        if (paper.getTotalScore() != null && paper.getTotalScore() <= 0) {
            throw new BusinessException("总分必须大于0");
        }

        paper.setStatus(0); // 草稿状态
        baseMapper.insert(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaper(Paper paper) {
        // 获取现有试卷信息
        Paper existingPaper = baseMapper.selectById(paper.getId());
        if (existingPaper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷编码是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Paper>()
                .eq(Paper::getPaperCode, paper.getPaperCode())
                .ne(Paper::getId, paper.getId())
        );
        if (count > 0) {
            throw new BusinessException("试卷编码已存在");
        }
        
        // 验证分数合理性
        Integer totalScore = paper.getTotalScore() != null ? paper.getTotalScore() : existingPaper.getTotalScore();
        Integer passScore = paper.getPassScore() != null ? paper.getPassScore() : existingPaper.getPassScore();
        if (passScore != null && totalScore != null) {
            if (passScore > totalScore) {
                throw new BusinessException("及格分不能大于总分");
            }
            if (passScore <= 0) {
                throw new BusinessException("及格分必须大于0");
            }
        }
        if (totalScore != null && totalScore <= 0) {
            throw new BusinessException("总分必须大于0");
        }

        baseMapper.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long id) {
        // 检查试卷是否有考试记录
        Long count = examRecordMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getPaperId, id)
        );
        if (count > 0) {
            throw new BusinessException("试卷存在考试记录，无法删除");
        }

        // 删除试卷关联的题目
        paperQuestionMapper.delete(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPaper(Long id) {
        // 获取现有试卷信息
        Paper existingPaper = baseMapper.selectById(id);
        if (existingPaper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷状态
        if (existingPaper.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的试卷可以发布");
        }
        
        // 检查试卷是否有关联题目
        Long questionCount = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );
        if (questionCount == 0) {
            throw new BusinessException("试卷没有关联题目，请先添加题目后再发布");
        }
        
        // 检查试卷总分和及格分是否已设置
        if (existingPaper.getTotalScore() == null || existingPaper.getTotalScore() <= 0) {
            throw new BusinessException("请先设置试卷总分");
        }
        if (existingPaper.getPassScore() == null || existingPaper.getPassScore() <= 0) {
            throw new BusinessException("请先设置及格分数");
        }
        
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(1); // 已发布
        baseMapper.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disablePaper(Long id) {
        // 获取现有试卷信息
        Paper existingPaper = baseMapper.selectById(id);
        if (existingPaper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷状态
        if (existingPaper.getStatus() != 1) {
            throw new BusinessException("只有已发布的试卷可以停用");
        }
        
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(2); // 已停用（与前端状态码保持一致）
        baseMapper.updateById(paper);
    }
}
