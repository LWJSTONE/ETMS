package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.entity.User;
import com.etms.entity.TrainingPlan;
import com.etms.exception.BusinessException;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.mapper.UserMapper;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.service.ExamRecordService;
import com.etms.service.UserService;
import com.etms.vo.ExamRecordVO;
import com.etms.vo.ExamResultVO;
import com.etms.vo.ExamResultStatsVO;
import com.etms.entity.Dept;
import com.etms.mapper.DeptMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分数计算结果内部类
 */
class ScoreResult {
    private final int score;
    private final boolean needManualScore;
    
    public ScoreResult(int score, boolean needManualScore) {
        this.score = score;
        this.needManualScore = needManualScore;
    }
    
    public int getScore() {
        return score;
    }
    
    public boolean isNeedManualScore() {
        return needManualScore;
    }
}

/**
 * 考试记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {
    
    private final PaperMapper paperMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final DeptMapper deptMapper;
    private final TrainingPlanMapper trainingPlanMapper;
    
    @Override
    public Page<ExamRecordVO> pageExamRecords(Page<ExamRecord> page, Long paperId, Long userId, Integer status, String userName, String paperName) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(status != null, ExamRecord::getStatus, status)
               .orderByDesc(ExamRecord::getCreateTime);
        
        // 修复分页问题：在查询前先获取符合条件的用户ID和试卷ID列表
        List<Long> matchedUserIds = null;
        List<Long> matchedPaperIds = null;
        
        if (userName != null && !userName.isEmpty()) {
            // 查询匹配用户名的用户ID
            matchedUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .like(User::getRealName, userName)
                    .or()
                    .like(User::getUsername, userName)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (matchedUserIds.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Page<ExamRecordVO> emptyPage = new Page<>();
                emptyPage.setRecords(Collections.emptyList());
                emptyPage.setTotal(0);
                return emptyPage;
            }
            wrapper.in(ExamRecord::getUserId, matchedUserIds);
        }
        
        if (paperName != null && !paperName.isEmpty()) {
            // 查询匹配试卷名的试卷ID
            matchedPaperIds = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>()
                    .like(Paper::getPaperName, paperName)
            ).stream().map(Paper::getId).collect(Collectors.toList());
            
            if (matchedPaperIds.isEmpty()) {
                // 没有匹配的试卷，返回空结果
                Page<ExamRecordVO> emptyPage = new Page<>();
                emptyPage.setRecords(Collections.emptyList());
                emptyPage.setTotal(0);
                return emptyPage;
            }
            wrapper.in(ExamRecord::getPaperId, matchedPaperIds);
        }
        
        Page<ExamRecord> recordPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ExamRecordVO> voPage = new Page<>();
        BeanUtils.copyProperties(recordPage, voPage, "records");
        
        // 批量获取试卷和用户信息
        List<Long> paperIds = recordPage.getRecords().stream()
                .map(ExamRecord::getPaperId)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> userIds = recordPage.getRecords().stream()
                .map(ExamRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Paper> paperMap = paperIds.isEmpty() ? Collections.emptyMap() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(Collectors.toMap(Paper::getId, p -> p));
        
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        
        // 转换（不再需要内存过滤）
        List<ExamRecordVO> voList = recordPage.getRecords().stream()
                .map(record -> {
                    ExamRecordVO vo = new ExamRecordVO();
                    BeanUtils.copyProperties(record, vo);
                    
                    Paper paper = paperMap.get(record.getPaperId());
                    if (paper != null) {
                        vo.setPaperName(paper.getPaperName());
                        vo.setTotalScore(paper.getTotalScore());
                        vo.setPassScore(paper.getPassScore());
                        vo.setDuration(paper.getExamDuration());
                    }
                    
                    User user = userMap.get(record.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getUsername());
                        vo.setRealName(user.getRealName());
                    }
                    
                    return vo;
                }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public ExamRecordVO getExamRecordDetail(Long id) {
        ExamRecord record = baseMapper.selectById(id);
        if (record == null) {
            return null;
        }
        
        // 权限校验：用户只能查看自己的考试记录，管理员可以查看所有
        User currentUser = userService.getCurrentUser();
        if (currentUser != null && !record.getUserId().equals(currentUser.getId())) {
            // 检查是否是管理员或培训管理员
            if (!hasAdminRole(currentUser)) {
                throw new BusinessException("无权查看此考试记录");
            }
        }
        
        ExamRecordVO vo = new ExamRecordVO();
        BeanUtils.copyProperties(record, vo);
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper != null) {
            vo.setPaperName(paper.getPaperName());
            vo.setTotalScore(paper.getTotalScore());
            vo.setPassScore(paper.getPassScore());
            vo.setDuration(paper.getExamDuration());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        
        return vo;
    }
    
    @Override
    public ExamRecordVO getExamRecordDetailForUser(Long id) {
        ExamRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 权限校验：用户只能查看自己的考试记录，管理员可以查看所有
        if (!hasAdminRole(currentUser) && !record.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权查看此考试记录");
        }
        
        ExamRecordVO vo = new ExamRecordVO();
        BeanUtils.copyProperties(record, vo);
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper != null) {
            vo.setPaperName(paper.getPaperName());
            vo.setTotalScore(paper.getTotalScore());
            vo.setPassScore(paper.getPassScore());
            vo.setDuration(paper.getExamDuration());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setRealName(user.getRealName());
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamRecord startExam(Long paperId, Long planId) {
        // 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查试卷是否存在
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷状态
        if (paper.getStatus() != 1) {
            throw new BusinessException("试卷未发布，无法开始考试");
        }
        
        // 检查考试时间窗口
        LocalDateTime now = LocalDateTime.now();
        if (paper.getStartTime() != null && now.isBefore(paper.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (paper.getEndTime() != null && now.isAfter(paper.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        
        // 检查是否已有进行中的考试（状态0表示考试中）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUser.getId())
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStatus, 0)
        );
        if (count > 0) {
            throw new BusinessException("您已有进行中的考试，请先完成");
        }
        
        // 修复：检查考试次数限制（补考次数限制）
        if (planId != null) {
            TrainingPlan plan = trainingPlanMapper.selectById(planId);
            if (plan != null && plan.getMaxRetake() != null && plan.getMaxRetake() > 0) {
                // 修复：统计用户已完成的考试次数，包括已提交(1)、超时(2)、已批阅(3)、已放弃(4)状态
                // 注意：状态定义：0-考试中 1-已提交 2-超时 3-已批阅 4-已放弃
                Long totalAttempts = baseMapper.selectCount(
                    new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, currentUser.getId())
                        .eq(ExamRecord::getPaperId, paperId)
                        .eq(ExamRecord::getPlanId, planId)
                        .in(ExamRecord::getStatus, 1, 2, 3, 4) // 已提交、超时、已批阅、已放弃
                );
                if (totalAttempts >= plan.getMaxRetake()) {
                    throw new BusinessException("您已达到最大考试次数限制（" + plan.getMaxRetake() + "次）");
                }
            }
        }
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(currentUser.getId());
        record.setPaperId(paperId);
        record.setPlanId(planId);
        record.setStatus(0); // 考试中
        record.setTotalScore(paper.getTotalScore());
        record.setStartTime(LocalDateTime.now());
        
        baseMapper.insert(record);
        
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(Long recordId, String answers) {
        ExamRecord record = baseMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        // 验证考试状态（状态0表示考试中）
        if (record.getStatus() != 0) {
            throw new BusinessException("考试已结束");
        }
        
        // 验证是否为当前用户的记录
        User currentUser = userService.getCurrentUser();
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权提交此考试");
        }
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查考试是否超时
        if (record.getStartTime() != null && paper.getExamDuration() != null) {
            long minutesUsed = Duration.between(record.getStartTime(), LocalDateTime.now()).toMinutes();
            if (minutesUsed > paper.getExamDuration()) {
                // 修复问题：超时提交时不抛出异常，而是返回正常结果，让用户能看到分数
                int userScore = calculateScore(record.getPaperId(), answers);
                // 标记为超时并保存完整数据（状态2表示超时）
                record.setStatus(2); // 超时
                record.setUserScore(userScore);
                record.setPassed(userScore >= paper.getPassScore() ? 1 : 0);
                record.setAnswerDetail(answers); // 保存答题详情
                record.setSubmitTime(LocalDateTime.now());
                record.setDurationUsed((int) minutesUsed);
                baseMapper.updateById(record);
                // 不抛出异常，直接返回，让用户能看到自己的考试结果
                // 前端可以根据status=3判断是超时自动提交
                return;
            }
        }
        
        // 修复并发问题：使用乐观锁方式更新状态
        // 先尝试将状态从0（考试中）更新为1（已提交），如果更新失败说明已被其他线程处理
        int updateCount = baseMapper.updateStatusToSubmitted(recordId, 0, 1);
        if (updateCount == 0) {
            throw new BusinessException("考试已提交，请勿重复提交");
        }
        
        // 计算分数（简化处理，实际应根据答案计算）
        int userScore = calculateScore(record.getPaperId(), answers);
        
        // 计算实际用时（分钟）
        LocalDateTime submitTime = LocalDateTime.now();
        Integer durationUsed = null;
        if (record.getStartTime() != null) {
            Duration duration = Duration.between(record.getStartTime(), submitTime);
            durationUsed = (int) duration.toMinutes();
        }
        
        // 更新考试记录的其他信息
        ExamRecord updateRecord = new ExamRecord();
        updateRecord.setId(recordId);
        updateRecord.setUserScore(userScore);
        updateRecord.setPassed(userScore >= paper.getPassScore() ? 1 : 0);
        updateRecord.setSubmitTime(submitTime);
        updateRecord.setDurationUsed(durationUsed);
        updateRecord.setAnswerDetail(answers); // 保存答题详情
        
        baseMapper.updateById(updateRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void giveUpExam(Long recordId) {
        ExamRecord record = baseMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        
        // 验证考试状态（状态0表示考试中）
        if (record.getStatus() != 0) {
            throw new BusinessException("考试已结束，无法放弃");
        }
        
        // 验证是否为当前用户的记录
        User currentUser = userService.getCurrentUser();
        if (!record.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权操作此考试");
        }
        
        // 更新考试记录状态为已放弃（状态4表示已放弃）
        // 修复并发问题：使用乐观锁方式更新状态
        int updateCount = baseMapper.updateStatusToSubmitted(recordId, 0, 4);
        if (updateCount == 0) {
            throw new BusinessException("考试状态已变更，无法放弃");
        }
        
        // 更新其他信息
        ExamRecord updateRecord = new ExamRecord();
        updateRecord.setId(recordId);
        updateRecord.setSubmitTime(LocalDateTime.now());
        
        // 计算实际用时（分钟）
        if (record.getStartTime() != null) {
            Duration duration = Duration.between(record.getStartTime(), LocalDateTime.now());
            updateRecord.setDurationUsed((int) duration.toMinutes());
        }
        
        baseMapper.updateById(updateRecord);
    }
    
    @Override
    public Page<ExamRecordVO> pageMyExamRecords(Page<ExamRecord> page, Integer status) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        return pageExamRecords(page, null, currentUser.getId(), status, null, null);
    }
    
    /**
     * 计算考试分数（保持原有接口兼容性）
     * @param paperId 试卷ID
     * @param answers 用户答案（JSON格式）
     * @return 用户得分
     */
    @Override
    public int calculateScore(Long paperId, String answers) {
        ScoreResult result = calculateScoreWithFlag(paperId, answers);
        return result.getScore();
    }
    
    /**
     * 计算考试分数并返回是否需要人工评分标记
     * @param paperId 试卷ID
     * @param answers 用户答案（JSON格式）
     * @return 分数计算结果（包含分数和待人工评分标记）
     */
    public ScoreResult calculateScoreWithFlag(Long paperId, String answers) {
        if (answers == null || answers.isEmpty()) {
            return new ScoreResult(0, false);
        }
        
        try {
            // 解析用户答案
            List<Map<String, Object>> answerList = objectMapper.readValue(answers, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            
            if (answerList.isEmpty()) {
                return new ScoreResult(0, false);
            }
            
            // 获取试卷关联的题目
            List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                    .eq(PaperQuestion::getPaperId, paperId)
                    .orderByAsc(PaperQuestion::getSortOrder)
            );
            
            if (paperQuestions.isEmpty()) {
                return new ScoreResult(0, false);
            }
            
            // 获取所有题目ID
            List<Long> questionIds = paperQuestions.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toList());
            
            // 批量查询题目
            List<Question> questions = questionMapper.selectBatchIds(questionIds);
            Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
            
            // 构建题目ID与分数的映射
            Map<Long, Integer> scoreMap = paperQuestions.stream()
                .collect(Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getScore));
            
            // 计算总分
            int totalScore = 0;
            boolean hasEssayQuestion = false; // 简答题标记
            for (Map<String, Object> answer : answerList) {
                // 空值检查
                Object questionIdObj = answer.get("questionId");
                if (questionIdObj == null) {
                    continue;
                }
                Long questionId;
                try {
                    questionId = Long.valueOf(questionIdObj.toString());
                } catch (NumberFormatException e) {
                    log.warn("无效的题目ID格式: {}", questionIdObj);
                    continue;
                }
                String userAnswer = answer.get("userAnswer") != null ? answer.get("userAnswer").toString().trim() : "";
                
                Question question = questionMap.get(questionId);
                if (question == null || userAnswer.isEmpty()) {
                    continue;
                }
                
                // 比较答案（忽略大小写和空格）
                String correctAnswer = question.getAnswer() != null ? question.getAnswer().trim() : "";
                
                // 判断题类型处理
                if (question.getQuestionType() == 3) {
                    // 判断题：将"正确/错误"与"A/B"互相转换
                    userAnswer = normalizeJudgeAnswer(userAnswer);
                    correctAnswer = normalizeJudgeAnswer(correctAnswer);
                }
                
                // 单选题和多选题：答案可能是A,B,C格式
                if (question.getQuestionType() == 1 || question.getQuestionType() == 2) {
                    userAnswer = userAnswer.toUpperCase().replaceAll("[\\s,]+", ",");
                    correctAnswer = correctAnswer.toUpperCase().replaceAll("[\\s,]+", ",");
                    // 移除首尾逗号
                    userAnswer = userAnswer.replaceAll("^,|,$", "");
                    correctAnswer = correctAnswer.replaceAll("^,|,$", "");
                    
                    // 多选题：对答案字符排序后再比较，解决答案顺序敏感问题
                    if (question.getQuestionType() == 2) {
                        userAnswer = sortAnswerChars(userAnswer);
                        correctAnswer = sortAnswerChars(correctAnswer);
                    }
                }
                
                // 填空题(类型4)：支持多个正确答案（用|分隔），忽略大小写
                if (question.getQuestionType() == 4) {
                    String[] correctAnswers = correctAnswer.split("\\|");
                    boolean matched = false;
                    for (String ca : correctAnswers) {
                        if (userAnswer.equalsIgnoreCase(ca.trim())) {
                            matched = true;
                            break;
                        }
                    }
                    if (matched) {
                        Integer score = scoreMap.get(questionId);
                        totalScore += score != null ? score : 0;
                    }
                    continue; // 填空题已处理，跳过后续判断
                }
                
                // 简答题(类型5)：标记为待人工评分，此处暂不自动评分
                // 实际应用中可通过关键词匹配或人工评分
                if (question.getQuestionType() == 5) {
                    // 简答题需要人工评分，默认给0分，记录日志提示管理员进行人工评分
                    log.info("简答题[ID:{}]需要人工评分，用户答案: {}", questionId, userAnswer);
                    // 标记有简答题需要人工评分
                    hasEssayQuestion = true;
                    // 简答题默认给0分，后续由管理员手动评分
                    continue;
                }
                
                // 判断答案是否正确
                if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                    Integer score = scoreMap.get(questionId);
                    totalScore += score != null ? score : 0;
                }
            }
            
            return new ScoreResult(totalScore, hasEssayQuestion);
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // 修复问题：分数计算失败时应记录日志并抛出异常，而非静默返回0分
            log.error("计算分数失败，试卷ID: {}, 答案内容: {}, 错误信息: {}", paperId, answers, e.getMessage(), e);
            throw new BusinessException("分数计算失败：" + e.getMessage());
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
     * 对多选题答案字符进行排序，解决答案顺序敏感问题
     * 例如：将 "C,B,A" 或 "CBA" 排序为 "A,B,C"
     */
    private String sortAnswerChars(String answer) {
        if (answer == null || answer.isEmpty()) {
            return answer;
        }
        // 将答案按逗号分割，排序后再用逗号连接
        String[] parts = answer.split(",");
        java.util.Arrays.sort(parts);
        return String.join(",", parts);
    }
    
    @Override
    public Page<ExamResultVO> pageResults(Long current, Long size, Long paperId, Long userId, Integer passed, String userName, String paperName, String startTime, String endTime) {
        Page<ExamRecord> page = new Page<>(current, size);
        
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        // 修复：查询已提交(1)、超时(2)、已批阅(3)和已放弃(4)状态的考试记录
        // 状态定义：0-考试中 1-已提交 2-超时 3-已批阅 4-已放弃
        wrapper.in(ExamRecord::getStatus, 1, 2, 3, 4)
               .eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(passed != null, ExamRecord::getPassed, passed)
               .orderByDesc(ExamRecord::getSubmitTime);
        
        // 修复：添加时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(ExamRecord::getSubmitTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(ExamRecord::getSubmitTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        
        // 修复分页性能问题：在SQL层面过滤用户名和试卷名，避免内存过滤导致分页不准确
        // 查询匹配用户名的用户ID列表
        List<Long> matchedUserIds = null;
        if (userName != null && !userName.isEmpty()) {
            matchedUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .like(User::getRealName, userName)
                    .or()
                    .like(User::getUsername, userName)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (matchedUserIds.isEmpty()) {
                // 没有匹配的用户，返回空结果
                Page<ExamResultVO> emptyPage = new Page<>(current, size, 0);
                emptyPage.setRecords(new java.util.ArrayList<>());
                return emptyPage;
            }
            wrapper.in(ExamRecord::getUserId, matchedUserIds);
        }
        
        // 查询匹配试卷名的试卷ID列表
        List<Long> matchedPaperIds = null;
        if (paperName != null && !paperName.isEmpty()) {
            matchedPaperIds = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>()
                    .like(Paper::getPaperName, paperName)
            ).stream().map(Paper::getId).collect(Collectors.toList());
            
            if (matchedPaperIds.isEmpty()) {
                // 没有匹配的试卷，返回空结果
                Page<ExamResultVO> emptyPage = new Page<>(current, size, 0);
                emptyPage.setRecords(new java.util.ArrayList<>());
                return emptyPage;
            }
            wrapper.in(ExamRecord::getPaperId, matchedPaperIds);
        }
        
        Page<ExamRecord> recordPage = baseMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ExamResultVO> voPage = new Page<>(current, size, recordPage.getTotal());
        
        if (recordPage.getRecords().isEmpty()) {
            voPage.setRecords(new java.util.ArrayList<>());
            return voPage;
        }
        
        // 批量获取试卷和用户信息
        java.util.Set<Long> paperIds = recordPage.getRecords().stream()
                .map(ExamRecord::getPaperId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<Long> userIds = recordPage.getRecords().stream()
                .map(ExamRecord::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Map<Long, Paper> paperMap = paperIds.isEmpty() ? new java.util.HashMap<>() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Paper::getId, p -> p));
        
        java.util.Map<Long, User> userMap = userIds.isEmpty() ? new java.util.HashMap<>() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        
        // 批量获取部门信息
        java.util.Set<Long> deptIds = userMap.values().stream()
                .filter(u -> u.getDeptId() != null)
                .map(User::getDeptId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Map<Long, String> deptNameMap = deptIds.isEmpty() ? new java.util.HashMap<>() :
                deptMapper.selectBatchIds(deptIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Dept::getId, Dept::getDeptName));
        
        // 转换（不再需要内存过滤，过滤条件已在SQL层面处理）
        java.util.List<ExamResultVO> voList = recordPage.getRecords().stream()
                .map(record -> {
                    ExamResultVO vo = new ExamResultVO();
                    BeanUtils.copyProperties(record, vo);
                    
                    Paper paper = paperMap.get(record.getPaperId());
                    if (paper != null) {
                        vo.setPaperName(paper.getPaperName());
                        vo.setTotalScore(paper.getTotalScore());
                        vo.setPassScore(paper.getPassScore());
                        vo.setExamDuration(paper.getExamDuration());
                    }
                    
                    User user = userMap.get(record.getUserId());
                    if (user != null) {
                        vo.setUserName(user.getUsername());
                        vo.setRealName(user.getRealName());
                        vo.setDeptId(user.getDeptId());
                        vo.setDeptName(deptNameMap.get(user.getDeptId()));
                    }
                    
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public Page<ExamResultVO> getMyResults(Long current, Long size, Integer passed, String paperName, String startTime, String endTime) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        return pageResults(current, size, null, currentUser.getId(), passed, null, paperName, startTime, endTime);
    }
    
    @Override
    public ExamResultVO getResultDetail(Long id) {
        ExamRecord record = baseMapper.selectById(id);
        // 修复：支持已提交(1)、超时(2)、已批阅(3)和已放弃(4)状态的记录
        if (record == null || (record.getStatus() != 1 && record.getStatus() != 2 && record.getStatus() != 3 && record.getStatus() != 4)) {
            return null;
        }
        
        // 权限校验：只能查看自己的成绩，或者管理员可以查看所有
        User currentUser = userService.getCurrentUser();
        if (currentUser != null && !record.getUserId().equals(currentUser.getId())) {
            // 检查是否是管理员
            if (!hasAdminRole(currentUser)) {
                throw new BusinessException("无权查看他人成绩详情");
            }
        }
        
        ExamResultVO vo = new ExamResultVO();
        BeanUtils.copyProperties(record, vo);
        
        // 获取试卷信息
        Paper paper = paperMapper.selectById(record.getPaperId());
        if (paper != null) {
            vo.setPaperName(paper.getPaperName());
            vo.setTotalScore(paper.getTotalScore());
            vo.setPassScore(paper.getPassScore());
            vo.setExamDuration(paper.getExamDuration());
        }
        
        // 获取用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setDeptId(user.getDeptId());
            if (user.getDeptId() != null) {
                Dept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
        }
        
        return vo;
    }
    
    @Override
    public ExamResultStatsVO getResultStats(String startTime, String endTime) {
        ExamResultStatsVO stats = new ExamResultStatsVO();
        
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        // 修复：统计已提交(1)、超时(2)、已批阅(3)和已放弃(4)状态的考试
        wrapper.in(ExamRecord::getStatus, 1, 2, 3, 4);
        
        // 时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(ExamRecord::getSubmitTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(ExamRecord::getSubmitTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        
        // 统计总数
        Long totalCount = baseMapper.selectCount(wrapper);
        stats.setTotalCount(totalCount);
        
        if (totalCount == 0) {
            stats.setPassCount(0L);
            stats.setFailCount(0L);
            stats.setPassRate(0.0);
            stats.setAvgScore(0.0);
            return stats;
        }
        
        // 统计通过人数
        LambdaQueryWrapper<ExamRecord> passWrapper = new LambdaQueryWrapper<>();
        passWrapper.in(ExamRecord::getStatus, 1, 2, 3, 4)
                   .eq(ExamRecord::getPassed, 1);
        if (startTime != null && !startTime.isEmpty()) {
            passWrapper.ge(ExamRecord::getSubmitTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            passWrapper.le(ExamRecord::getSubmitTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        Long passCount = baseMapper.selectCount(passWrapper);
        stats.setPassCount(passCount);
        stats.setFailCount(totalCount - passCount);
        stats.setPassRate(totalCount > 0 ? (passCount * 100.0 / totalCount) : 0.0);
        
        // 计算平均分
        List<ExamRecord> records = baseMapper.selectList(wrapper);
        double avgScore = records.stream()
                .filter(r -> r.getUserScore() != null)
                .mapToDouble(r -> r.getUserScore().doubleValue())
                .average()
                .orElse(0.0);
        // 保留两位小数
        avgScore = Math.round(avgScore * 100.0) / 100.0;
        stats.setAvgScore(avgScore);
        
        return stats;
    }
    
    @Override
    public void exportRecords(Long paperId, Long userId, Integer status, String userName, String paperName, OutputStream outputStream) {
        // 修复：将过滤条件在SQL查询阶段完成，避免内存过滤
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(status != null, ExamRecord::getStatus, status)
               .orderByDesc(ExamRecord::getCreateTime);
        
        // 修复：在SQL层面过滤用户名
        if (userName != null && !userName.isEmpty()) {
            List<Long> matchedUserIds = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .like(User::getRealName, userName)
                    .or()
                    .like(User::getUsername, userName)
            ).stream().map(User::getId).collect(Collectors.toList());
            
            if (matchedUserIds.isEmpty()) {
                throw new BusinessException("没有可导出的数据");
            }
            wrapper.in(ExamRecord::getUserId, matchedUserIds);
        }
        
        // 修复：在SQL层面过滤试卷名
        if (paperName != null && !paperName.isEmpty()) {
            List<Long> matchedPaperIds = paperMapper.selectList(
                new LambdaQueryWrapper<Paper>()
                    .like(Paper::getPaperName, paperName)
            ).stream().map(Paper::getId).collect(Collectors.toList());
            
            if (matchedPaperIds.isEmpty()) {
                throw new BusinessException("没有可导出的数据");
            }
            wrapper.in(ExamRecord::getPaperId, matchedPaperIds);
        }
        
        List<ExamRecord> records = baseMapper.selectList(wrapper);
        
        if (records.isEmpty()) {
            throw new BusinessException("没有可导出的数据");
        }
        
        // 获取关联信息
        java.util.Set<Long> paperIds = records.stream()
                .map(ExamRecord::getPaperId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<Long> userIds = records.stream()
                .map(ExamRecord::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Map<Long, Paper> paperMap = paperIds.isEmpty() ? new java.util.HashMap<>() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Paper::getId, p -> p));
        
        java.util.Map<Long, User> userMap = userIds.isEmpty() ? new java.util.HashMap<>() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        
        // 使用EasyExcel或Apache POI导出（使用try-with-resources确保资源正确释放）
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("考试记录");
            
            // 创建表头
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"考生姓名", "试卷名称", "考试时长(分钟)", "得分", "是否通过", "考试状态", "开始时间", "提交时间"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // 填充数据（过滤条件已在SQL层面处理，无需内存过滤）
            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (ExamRecord record : records) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                User user = userMap.get(record.getUserId());
                Paper paper = paperMap.get(record.getPaperId());
                
                row.createCell(0).setCellValue(user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "");
                row.createCell(1).setCellValue(paper != null ? paper.getPaperName() : "");
                row.createCell(2).setCellValue(paper != null && paper.getExamDuration() != null ? paper.getExamDuration() : 0);
                row.createCell(3).setCellValue(record.getUserScore() != null ? record.getUserScore() : 0);
                row.createCell(4).setCellValue(record.getPassed() != null && record.getPassed() == 1 ? "通过" : "未通过");
                row.createCell(5).setCellValue(getStatusName(record.getStatus()));
                row.createCell(6).setCellValue(record.getStartTime() != null ? record.getStartTime().format(dateFormatter) : "");
                row.createCell(7).setCellValue(record.getSubmitTime() != null ? record.getSubmitTime().format(dateFormatter) : "");
            }
            
            // 写入输出流
            workbook.write(outputStream);
        } catch (Exception e) {
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "考试中";
            case 1: return "已提交";
            case 2: return "超时";
            case 3: return "已批阅";
            case 4: return "已放弃";
            default: return "未知";
        }
    }
    
    @Override
    public void exportResults(Long paperId, Long userId, Integer passed, String userName, String paperName, String startTime, String endTime, OutputStream outputStream) {
        // 构建查询条件
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        // 修复：导出已完成(2)和已超时(3)状态的考试
        wrapper.in(ExamRecord::getStatus, 2, 3)
               .eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(passed != null, ExamRecord::getPassed, passed)
               .orderByDesc(ExamRecord::getSubmitTime);
        
        // 时间范围过滤
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(ExamRecord::getSubmitTime, LocalDateTime.parse(startTime + "T00:00:00"));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(ExamRecord::getSubmitTime, LocalDateTime.parse(endTime + "T23:59:59"));
        }
        
        List<ExamRecord> records = baseMapper.selectList(wrapper);
        
        if (records.isEmpty()) {
            throw new BusinessException("没有可导出的数据");
        }
        
        // 获取关联信息
        java.util.Set<Long> paperIds = records.stream()
                .map(ExamRecord::getPaperId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<Long> userIds = records.stream()
                .map(ExamRecord::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Map<Long, Paper> paperMap = paperIds.isEmpty() ? new java.util.HashMap<>() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Paper::getId, p -> p));
        
        java.util.Map<Long, User> userMap = userIds.isEmpty() ? new java.util.HashMap<>() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getId, u -> u));
        
        // 批量获取部门信息
        java.util.Set<Long> deptIds = userMap.values().stream()
                .filter(u -> u.getDeptId() != null)
                .map(User::getDeptId)
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Map<Long, String> deptNameMap = deptIds.isEmpty() ? new java.util.HashMap<>() :
                deptMapper.selectBatchIds(deptIds).stream()
                        .collect(java.util.stream.Collectors.toMap(Dept::getId, Dept::getDeptName));
        
        // 使用Apache POI导出Excel（使用try-with-resources确保资源正确释放）
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("成绩报表");
            
            // 创建表头
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"考生姓名", "所属部门", "试卷名称", "得分", "满分", "及格分", "是否通过", "补考次数", "考试时间", "用时(分钟)"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            
            // 填充数据
            int rowNum = 1;
            // 修复问题：SimpleDateFormat非线程安全，改用DateTimeFormatter
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (ExamRecord record : records) {
                // 过滤用户名
                if (userName != null && !userName.isEmpty()) {
                    User user = userMap.get(record.getUserId());
                    if (user == null || 
                        (user.getRealName() == null || !user.getRealName().contains(userName)) &&
                        (user.getUsername() == null || !user.getUsername().contains(userName))) {
                        continue;
                    }
                }
                // 过滤试卷名
                if (paperName != null && !paperName.isEmpty()) {
                    Paper paper = paperMap.get(record.getPaperId());
                    if (paper == null || paper.getPaperName() == null || !paper.getPaperName().contains(paperName)) {
                        continue;
                    }
                }
                
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                User user = userMap.get(record.getUserId());
                Paper paper = paperMap.get(record.getPaperId());
                
                row.createCell(0).setCellValue(user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "");
                row.createCell(1).setCellValue(user != null ? deptNameMap.getOrDefault(user.getDeptId(), "") : "");
                row.createCell(2).setCellValue(paper != null ? paper.getPaperName() : "");
                row.createCell(3).setCellValue(record.getUserScore() != null ? record.getUserScore() : 0);
                row.createCell(4).setCellValue(paper != null && paper.getTotalScore() != null ? paper.getTotalScore() : 0);
                row.createCell(5).setCellValue(paper != null && paper.getPassScore() != null ? paper.getPassScore() : 0);
                row.createCell(6).setCellValue(record.getPassed() != null && record.getPassed() == 1 ? "通过" : "未通过");
                row.createCell(7).setCellValue(record.getRetakeCount() != null ? record.getRetakeCount() : 0);
                row.createCell(8).setCellValue(record.getSubmitTime() != null ? record.getSubmitTime().format(dateFormatter) : "");
                row.createCell(9).setCellValue(record.getDurationUsed() != null ? record.getDurationUsed() : 0);
            }
            
            // 写入输出流
            workbook.write(outputStream);
        } catch (Exception e) {
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }
    
    @Override
    public void checkExamEligibility(Long userId, Long paperId, Long planId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        
        // 检查试卷是否存在
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        
        // 检查试卷状态
        if (paper.getStatus() != 1) {
            throw new BusinessException("试卷未发布，无法参加考试");
        }
        
        // 检查考试时间窗口
        LocalDateTime now = LocalDateTime.now();
        if (paper.getStartTime() != null && now.isBefore(paper.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (paper.getEndTime() != null && now.isAfter(paper.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        
        // 检查是否已有进行中的考试
        // 修复：状态0表示考试中，状态1表示已提交，之前错误地使用了状态1
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStatus, 0) // 状态0表示考试中
        );
        if (count > 0) {
            throw new BusinessException("您已有进行中的考试，请先完成");
        }
        
        // 检查考试次数限制
        if (planId != null) {
            TrainingPlan plan = trainingPlanMapper.selectById(planId);
            if (plan != null && plan.getMaxRetake() != null && plan.getMaxRetake() > 0) {
                // 修复：统计已提交(1)、超时(2)、已批阅(3)、已放弃(4)状态的考试次数
                Long totalAttempts = baseMapper.selectCount(
                    new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getPaperId, paperId)
                        .eq(ExamRecord::getPlanId, planId)
                        .in(ExamRecord::getStatus, 1, 2, 3, 4) // 已提交、超时、已批阅、已放弃
                );
                if (totalAttempts >= plan.getMaxRetake()) {
                    throw new BusinessException("您已达到最大考试次数限制（" + plan.getMaxRetake() + "次）");
                }
            }
        }
    }
    
    /**
     * 检查用户是否具有管理员或培训管理员角色
     * 修复：统一权限检查逻辑，避免空指针异常
     * @param user 用户对象
     * @return 是否具有管理员权限
     */
    private boolean hasAdminRole(User user) {
        if (user == null) {
            return false;
        }
        // 安全检查：getRoles()可能返回null
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        return user.getRoles().stream()
            .anyMatch(r -> r != null && r.getRoleCode() != null && 
                ("ADMIN".equals(r.getRoleCode()) || "TRAINING_MANAGER".equals(r.getRoleCode())));
    }
}
