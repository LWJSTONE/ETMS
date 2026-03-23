package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.ExamRecord;
import com.etms.entity.Paper;
import com.etms.entity.PaperQuestion;
import com.etms.entity.Question;
import com.etms.entity.User;
import com.etms.exception.BusinessException;
import com.etms.mapper.ExamRecordMapper;
import com.etms.mapper.PaperMapper;
import com.etms.mapper.PaperQuestionMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.mapper.UserMapper;
import com.etms.service.ExamRecordService;
import com.etms.service.UserService;
import com.etms.vo.ExamRecordVO;
import com.etms.vo.ExamResultVO;
import com.etms.entity.Dept;
import com.etms.mapper.DeptMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试记录服务实现类
 */
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
    
    @Override
    public Page<ExamRecordVO> pageExamRecords(Page<ExamRecord> page, Long paperId, Long userId, Integer status, String userName, String paperName) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(status != null, ExamRecord::getStatus, status)
               .orderByDesc(ExamRecord::getCreateTime);
        
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
        
        Map<Long, Paper> paperMap = paperIds.isEmpty() ? Map.of() :
                paperMapper.selectBatchIds(paperIds).stream()
                        .collect(Collectors.toMap(Paper::getId, p -> p));
        
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        
        // 过滤并转换
        List<ExamRecordVO> voList = recordPage.getRecords().stream()
                .filter(record -> {
                    // 过滤用户名
                    if (userName != null && !userName.isEmpty()) {
                        User user = userMap.get(record.getUserId());
                        if (user == null || 
                            (user.getRealName() == null || !user.getRealName().contains(userName)) &&
                            (user.getUsername() == null || !user.getUsername().contains(userName))) {
                            return false;
                        }
                    }
                    // 过滤试卷名
                    if (paperName != null && !paperName.isEmpty()) {
                        Paper paper = paperMap.get(record.getPaperId());
                        if (paper == null || paper.getPaperName() == null || !paper.getPaperName().contains(paperName)) {
                            return false;
                        }
                    }
                    return true;
                })
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
        
        // 检查是否已有进行中的考试
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, currentUser.getId())
                .eq(ExamRecord::getPaperId, paperId)
                .eq(ExamRecord::getStatus, 1)
        );
        if (count > 0) {
            throw new BusinessException("您已有进行中的考试，请先完成");
        }
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setUserId(currentUser.getId());
        record.setPaperId(paperId);
        record.setPlanId(planId);
        record.setStatus(1); // 进行中
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
        
        // 验证考试状态
        if (record.getStatus() != 1) {
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
        
        // 计算分数（简化处理，实际应根据答案计算）
        // TODO: 实现详细的评分逻辑
        int userScore = calculateScore(record.getPaperId(), answers);
        
        // 更新考试记录
        record.setStatus(2); // 已完成
        record.setUserScore(userScore);
        record.setPassed(userScore >= paper.getPassScore() ? 1 : 0);
        record.setSubmitTime(LocalDateTime.now());
        
        baseMapper.updateById(record);
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
     * 计算考试分数
     * @param paperId 试卷ID
     * @param answers 用户答案（JSON格式）
     * @return 用户得分
     */
    private int calculateScore(Long paperId, String answers) {
        if (answers == null || answers.isEmpty()) {
            return 0;
        }
        
        try {
            // 解析用户答案
            List<Map<String, Object>> answerList = objectMapper.readValue(answers, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            
            if (answerList.isEmpty()) {
                return 0;
            }
            
            // 获取试卷关联的题目
            List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                    .eq(PaperQuestion::getPaperId, paperId)
                    .orderByAsc(PaperQuestion::getSortOrder)
            );
            
            if (paperQuestions.isEmpty()) {
                return 0;
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
            for (Map<String, Object> answer : answerList) {
                Long questionId = Long.valueOf(answer.get("questionId").toString());
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
                }
                
                // 判断答案是否正确
                if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                    Integer score = scoreMap.get(questionId);
                    totalScore += score != null ? score : 0;
                }
            }
            
            return totalScore;
        } catch (Exception e) {
            // 解析失败返回0分
            return 0;
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
    
    @Override
    public Page<ExamResultVO> pageResults(Long current, Long size, Long paperId, Long userId, Integer passed, String userName, String paperName, String startTime, String endTime) {
        Page<ExamRecord> page = new Page<>(current, size);
        
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getStatus, 2) // 只查询已完成的考试
               .eq(paperId != null, ExamRecord::getPaperId, paperId)
               .eq(userId != null, ExamRecord::getUserId, userId)
               .eq(passed != null, ExamRecord::getPassed, passed)
               .orderByDesc(ExamRecord::getSubmitTime);
        
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
        
        // 过滤和转换
        java.util.List<ExamResultVO> voList = recordPage.getRecords().stream()
                .filter(record -> {
                    // 过滤用户名
                    if (userName != null && !userName.isEmpty()) {
                        User user = userMap.get(record.getUserId());
                        if (user == null || 
                            (user.getRealName() == null || !user.getRealName().contains(userName)) &&
                            (user.getUsername() == null || !user.getUsername().contains(userName))) {
                            return false;
                        }
                    }
                    // 过滤试卷名
                    if (paperName != null && !paperName.isEmpty()) {
                        Paper paper = paperMap.get(record.getPaperId());
                        if (paper == null || paper.getPaperName() == null || !paper.getPaperName().contains(paperName)) {
                            return false;
                        }
                    }
                    return true;
                })
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
        if (record == null || record.getStatus() != 2) {
            return null;
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
}
