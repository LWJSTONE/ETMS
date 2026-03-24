package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.entity.TrainingPlan;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.service.PaperService;
import com.etms.service.UserService;
import com.etms.vo.PaperQuestionVO;
import com.etms.vo.PaperVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final TrainingPlanMapper trainingPlanMapper;
    private final UserService userService;

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
    public PaperVO getPaperDetail(Long id) {
        return getPaperDetail(id, false, null);
    }
    
    @Override
    public PaperVO getPaperDetail(Long id, boolean forExam) {
        return getPaperDetail(id, forExam, null);
    }
    
    @Override
    public PaperVO getPaperDetail(Long id, boolean forExam, Long planId) {
        Paper paper = baseMapper.selectById(id);
        if (paper == null) {
            return null;
        }

        // 考试场景下的权限验证
        if (forExam) {
            // 验证试卷状态
            if (paper.getStatus() != 1) {
                throw new BusinessException("试卷未发布，无法参加考试");
            }
            
            // 验证考试时间窗口
            LocalDateTime now = LocalDateTime.now();
            if (paper.getStartTime() != null && now.isBefore(paper.getStartTime())) {
                throw new BusinessException("考试尚未开始");
            }
            if (paper.getEndTime() != null && now.isAfter(paper.getEndTime())) {
                throw new BusinessException("考试已结束");
            }
            
            // 如果关联了培训计划，验证用户是否有考试资格
            if (planId != null) {
                validateExamEligibility(paper, planId);
            }
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
                    
                    // 考试场景下隐藏答案和解析
                    if (forExam) {
                        qvo.setAnswer(null);
                        qvo.setAnswerAnalysis(null);
                    }
                    
                    questionVOList.add(qvo);
                }
            }
            vo.setQuestions(questionVOList);
        } else {
            vo.setQuestions(new ArrayList<>());
        }

        return vo;
    }
    
    /**
     * 验证用户是否有考试资格
     * 修复问题：试卷详情接口权限控制不完善
     */
    private void validateExamEligibility(Paper paper, Long planId) {
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 获取培训计划
        TrainingPlan plan = trainingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 验证培训计划状态
        if (plan.getStatus() != 1 && plan.getStatus() != 2) {
            throw new BusinessException("培训计划未发布或未开始");
        }
        
        // 验证培训计划时间窗口
        LocalDate today = LocalDate.now();
        if (plan.getStartDate() != null && today.isBefore(plan.getStartDate())) {
            throw new BusinessException("培训计划尚未开始");
        }
        if (plan.getEndDate() != null && today.isAfter(plan.getEndDate())) {
            throw new BusinessException("培训计划已结束");
        }
        
        // 验证用户是否在培训计划目标范围内
        if (!isUserInPlanTarget(currentUser, plan)) {
            throw new BusinessException("您不在该培训计划的目标范围内");
        }
        
        // 检查是否已有进行中的考试
        Long inProgressCount = examRecordMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUser.getId())
                .eq(ExamRecord::getPaperId, paper.getId())
                .eq(ExamRecord::getStatus, 1)
        );
        if (inProgressCount > 0) {
            throw new BusinessException("您已有进行中的考试，请先完成后再获取新试卷");
        }
        
        // 检查考试次数限制
        if (plan.getMaxRetake() != null && plan.getMaxRetake() > 0) {
            Long completedAttempts = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getUserId, currentUser.getId())
                    .eq(ExamRecord::getPaperId, paper.getId())
                    .eq(ExamRecord::getPlanId, planId)
                    .in(ExamRecord::getStatus, 2, 3)
            );
            if (completedAttempts >= plan.getMaxRetake()) {
                throw new BusinessException("您已达到最大考试次数限制");
            }
        }
    }
    
    /**
     * 检查用户是否在培训计划目标范围内
     */
    private boolean isUserInPlanTarget(User user, TrainingPlan plan) {
        Integer targetType = plan.getTargetType();
        if (targetType == null) {
            return true; // 没有设置目标类型，默认允许
        }
        
        try {
            switch (targetType) {
                case 1: // 部门
                    String targetDeptIds = plan.getTargetDeptIds();
                    if (targetDeptIds == null || targetDeptIds.trim().isEmpty()) {
                        return true;
                    }
                    if (user.getDeptId() == null) {
                        return false;
                    }
                    return isIdInJsonArray(targetDeptIds, user.getDeptId());
                    
                case 2: // 岗位
                    String targetPositionIds = plan.getTargetPositionIds();
                    if (targetPositionIds == null || targetPositionIds.trim().isEmpty()) {
                        return true;
                    }
                    if (user.getPositionId() == null) {
                        return false;
                    }
                    return isIdInJsonArray(targetPositionIds, user.getPositionId());
                    
                case 3: // 个人
                    String targetUserIds = plan.getTargetUserIds();
                    if (targetUserIds == null || targetUserIds.trim().isEmpty()) {
                        return true;
                    }
                    return isIdInJsonArray(targetUserIds, user.getId());
                    
                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查ID是否在JSON数组字符串中
     */
    private boolean isIdInJsonArray(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return false;
        }
        
        String str = jsonArrayStr.trim();
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }
        
        String[] parts = str.split(",");
        for (String part : parts) {
            String idStr = part.trim().replace("\"", "");
            if (idStr.equals(String.valueOf(targetId))) {
                return true;
            }
        }
        return false;
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
        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );
        if (paperQuestions.isEmpty()) {
            throw new BusinessException("试卷没有关联题目，请先添加题目后再发布");
        }
        
        // 检查试卷总分和及格分是否已设置
        if (existingPaper.getTotalScore() == null || existingPaper.getTotalScore() <= 0) {
            throw new BusinessException("请先设置试卷总分");
        }
        if (existingPaper.getPassScore() == null || existingPaper.getPassScore() <= 0) {
            throw new BusinessException("请先设置及格分数");
        }
        
        // 校验每道题是否都有分数设置
        boolean hasZeroScore = paperQuestions.stream()
            .anyMatch(pq -> pq.getScore() == null || pq.getScore() <= 0);
        if (hasZeroScore) {
            throw new BusinessException("存在未设置分数的题目，请先设置所有题目分数");
        }
        
        // 校验题目分数总和是否等于试卷总分
        int totalQuestionScore = paperQuestions.stream()
            .mapToInt(PaperQuestion::getScore)
            .sum();
        if (totalQuestionScore != existingPaper.getTotalScore()) {
            throw new BusinessException("题目分数总和(" + totalQuestionScore + ")与试卷总分(" + existingPaper.getTotalScore() + ")不一致");
        }
        
        // 修复：校验题目数量是否与试卷设置的题目数量一致
        if (existingPaper.getQuestionCount() != null && paperQuestions.size() != existingPaper.getQuestionCount()) {
            throw new BusinessException("实际题目数量(" + paperQuestions.size() + ")与试卷设置的题目数量(" + existingPaper.getQuestionCount() + ")不一致");
        }
        
        // 修复：使用乐观锁防止并发重复发布
        int updateCount = baseMapper.updateStatusWithOptimisticLock(id, 0, 1);
        if (updateCount == 0) {
            throw new BusinessException("发布失败，试卷状态已被修改，请刷新后重试");
        }
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
        
        // 修复并发问题：使用乐观锁方式更新状态，避免并发操作导致状态不一致
        int updateCount = baseMapper.disablePaperWithOptimisticLock(id, 1, 2);
        if (updateCount == 0) {
            throw new BusinessException("停用失败，试卷状态已被修改，请刷新后重试");
        }
    }
    
    @Override
    public boolean hasExamRecords(Long paperId) {
        if (paperId == null) {
            return false;
        }
        Long recordCount = examRecordMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getPaperId, paperId)
        );
        return recordCount != null && recordCount > 0;
    }
    
    // ==================== 组卷管理方法实现 ====================
    
    @Override
    public List<PaperQuestionVO> getPaperQuestions(Long paperId) {
        // 查询试卷关联的题目
        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
            new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId)
                .orderByAsc(PaperQuestion::getSortOrder)
        );
        
        if (paperQuestions.isEmpty()) {
            return new ArrayList<>();
        }
        
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
        
        return questionVOList;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestions(Long paperId, List<Map<String, Object>> questions) {
        // 验证试卷是否存在
        Paper paper = baseMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 验证试卷状态（只有草稿状态的试卷可以添加题目）
        if (paper.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的试卷可以添加题目");
        }
        
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException("题目列表不能为空");
        }
        
        // 获取当前最大排序号
        Integer maxSortOrder = paperQuestionMapper.selectMaxSortOrderByPaperId(paperId);
        int nextSortOrder = (maxSortOrder != null ? maxSortOrder : 0) + 1;
        
        // 批量添加题目
        for (Map<String, Object> item : questions) {
            Long questionId = Long.valueOf(item.get("questionId").toString());
            Integer score = item.get("score") != null ? Integer.valueOf(item.get("score").toString()) : 1;
            Integer sortOrder = item.get("sortOrder") != null ? Integer.valueOf(item.get("sortOrder").toString()) : nextSortOrder;
            
            // 验证题目是否存在
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                throw new BusinessException("题目ID " + questionId + " 不存在");
            }
            
            // 检查题目是否已存在于试卷中
            Long existCount = paperQuestionMapper.selectCount(
                new LambdaQueryWrapper<PaperQuestion>()
                    .eq(PaperQuestion::getPaperId, paperId)
                    .eq(PaperQuestion::getQuestionId, questionId)
            );
            if (existCount > 0) {
                continue; // 已存在则跳过
            }
            
            // 添加题目到试卷
            PaperQuestion paperQuestion = new PaperQuestion();
            paperQuestion.setPaperId(paperId);
            paperQuestion.setQuestionId(questionId);
            paperQuestion.setScore(score);
            paperQuestion.setSortOrder(sortOrder);
            paperQuestionMapper.insert(paperQuestion);
            
            nextSortOrder++;
        }
        
        // 更新试卷题目数量
        Long questionCount = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
        );
        paper.setQuestionCount(questionCount.intValue());
        baseMapper.updateById(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestionFromPaper(Long paperId, Long questionId) {
        // 验证试卷是否存在
        Paper paper = baseMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 验证试卷状态
        if (paper.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的试卷可以移除题目");
        }
        
        // 删除题目
        int deleted = paperQuestionMapper.delete(
            new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId)
                .eq(PaperQuestion::getQuestionId, questionId)
        );
        
        if (deleted == 0) {
            throw new BusinessException("题目不存在于试卷中");
        }
        
        // 更新试卷题目数量
        Long questionCount = paperQuestionMapper.selectCount(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
        );
        paper.setQuestionCount(questionCount.intValue());
        baseMapper.updateById(paper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearPaperQuestions(Long paperId) {
        // 验证试卷是否存在
        Paper paper = baseMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 验证试卷状态
        if (paper.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的试卷可以清空题目");
        }
        
        // 清空题目
        paperQuestionMapper.delete(
            new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
        );
        
        // 更新试卷题目数量为0
        paper.setQuestionCount(0);
        baseMapper.updateById(paper);
    }
}
