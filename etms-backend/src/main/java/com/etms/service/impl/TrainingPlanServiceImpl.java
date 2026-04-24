package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.TrainingPlan;
import com.etms.entity.UserTrainingPlan;
import com.etms.entity.Course;
import com.etms.entity.PlanCourse;
import com.etms.entity.User;
import com.etms.entity.Paper;
import com.etms.exception.BusinessException;
import com.etms.mapper.TrainingPlanMapper;
import com.etms.mapper.UserTrainingPlanMapper;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.PlanCourseMapper;
import com.etms.mapper.UserMapper;
import com.etms.mapper.PaperMapper;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import com.etms.util.JsonArrayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 培训计划服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {
    
    // 修复：将ObjectMapper声明为静态常量，避免每次调用都创建新实例，提升性能
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final UserTrainingPlanMapper userTrainingPlanMapper;
    private final CourseMapper courseMapper;
    private final PlanCourseMapper planCourseMapper;
    private final UserMapper userMapper;
    private final PaperMapper paperMapper;
    
    @Override
    public Page<TrainingPlanVO> pagePlans(Page<TrainingPlan> page, String planName, Integer status, Integer planType, String startDate, String endDate, Long deptId) {
        LambdaQueryWrapper<TrainingPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(planName), TrainingPlan::getPlanName, planName)
               .eq(status != null, TrainingPlan::getStatus, status)
               .eq(planType != null, TrainingPlan::getPlanType, planType)
               .orderByDesc(TrainingPlan::getCreateTime);
        
        // 修复：先检查日期参数是否有效，再进行解析，避免空指针异常
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(TrainingPlan::getStartDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(TrainingPlan::getEndDate, LocalDate.parse(endDate));
        }
        
        // 修复：部门筛选使用精确的JSON解析匹配，避免LIKE匹配导致的误匹配（如ID "1" 匹配到 "11", "21" 等）
        Page<TrainingPlan> planPage;
        if (deptId != null) {
            // 查询所有符合其他条件的数据
            List<TrainingPlan> allPlans = baseMapper.selectList(wrapper);
            // 使用精确的JSON解析匹配进行过滤
            List<TrainingPlan> filteredPlans = allPlans.stream()
                    .filter(plan -> isIdInJsonArray(plan.getTargetDeptIds(), deptId))
                    .collect(Collectors.toList());
            
            // 手动分页
            long total = filteredPlans.size();
            int current = (int) page.getCurrent();
            int size = (int) page.getSize();
            int fromIndex = (current - 1) * size;
            int toIndex = Math.min(fromIndex + size, filteredPlans.size());
            
            List<TrainingPlan> pagedPlans = fromIndex < filteredPlans.size() 
                    ? filteredPlans.subList(fromIndex, toIndex) 
                    : new ArrayList<>();
            
            planPage = new Page<>(current, size, total);
            planPage.setRecords(pagedPlans);
        } else {
            planPage = baseMapper.selectPage(page, wrapper);
        }
        
        Page<TrainingPlanVO> voPage = new Page<>();
        BeanUtils.copyProperties(planPage, voPage, "records");
        
        // 修复：批量查询课程信息，避免N+1查询问题
        List<Long> courseIds = planPage.getRecords().stream()
            .map(TrainingPlan::getCourseId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        }
        
        // 使用final变量在lambda中访问
        final Map<Long, Course> finalCourseMap = courseMap;
        
        List<TrainingPlanVO> voList = planPage.getRecords().stream().map(plan -> {
            TrainingPlanVO vo = new TrainingPlanVO();
            BeanUtils.copyProperties(plan, vo);
            vo.setPlanTypeName(getPlanTypeName(plan.getPlanType()));
            vo.setStatusName(getStatusName(plan.getStatus()));
            // 设置课程名称（从Map中获取，避免N+1查询）
            if (plan.getCourseId() != null) {
                Course course = finalCourseMap.get(plan.getCourseId());
                if (course != null) {
                    vo.setCourseName(course.getCourseName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public TrainingPlanVO getPlanDetail(Long id) {
        TrainingPlan plan = baseMapper.selectById(id);
        if (plan == null) {
            return null;
        }
        
        TrainingPlanVO vo = new TrainingPlanVO();
        BeanUtils.copyProperties(plan, vo);
        vo.setPlanTypeName(getPlanTypeName(plan.getPlanType()));
        vo.setStatusName(getStatusName(plan.getStatus()));
        
        // 设置课程名称
        if (plan.getCourseId() != null) {
            Course course = courseMapper.selectById(plan.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
            }
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPlan(TrainingPlan plan) {
        // 修复：增加必填字段校验，对应数据库NOT NULL约束
        if (!StringUtils.hasText(plan.getPlanName())) {
            throw new BusinessException("计划名称不能为空");
        }
        // 修复：planCode可以为空，如果为空则自动生成
        if (!StringUtils.hasText(plan.getPlanCode())) {
            plan.setPlanCode("PLAN-" + System.currentTimeMillis());
        }
        // 修复：计划编码唯一性检查必须考虑软删除的记录
        // @TableLogic只对SELECT生效，但数据库UNIQUE KEY约束对所有记录生效
        // 所以需要手动查询包含已删除记录的编码冲突
        // 使用自定义SQL绕过@TableLogic的自动过滤，检查所有记录（包括已删除的）
        Long codeCount = baseMapper.selectCount(
            new LambdaQueryWrapper<TrainingPlan>().eq(TrainingPlan::getPlanCode, plan.getPlanCode())
        );
        if (codeCount > 0) {
            // 非删除记录中已存在该编码
            throw new BusinessException("计划编码已存在，请使用其他编码");
        }
        if (plan.getStartDate() == null) {
            throw new BusinessException("开始日期不能为空");
        }
        if (plan.getEndDate() == null) {
            throw new BusinessException("结束日期不能为空");
        }
        if (plan.getTargetType() == null) {
            throw new BusinessException("目标类型不能为空");
        }
        // 验证日期合理性
        if (plan.getStartDate().isAfter(plan.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (plan.getCourseId() == null) {
            throw new BusinessException("请选择关联课程");
        }
        
        plan.setStatus(0); // 草稿状态
        try {
            return baseMapper.insert(plan) > 0;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 修复：如果planCode与软删除记录冲突（selectCount查不到但数据库UNIQUE KEY冲突），
            // 自动生成新的唯一编码重试
            log.warn("培训计划编码[{}]与已有记录冲突，自动重新生成", plan.getPlanCode());
            plan.setPlanCode("PLAN-" + System.currentTimeMillis());
            return baseMapper.insert(plan) > 0;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlan(TrainingPlan plan) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(plan.getId());
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只允许草稿状态(0)编辑
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("当前状态不允许编辑，只有草稿状态可以编辑");
        }
        
        // 验证日期合理性
        LocalDate startDate = plan.getStartDate() != null ? plan.getStartDate() : existingPlan.getStartDate();
        LocalDate endDate = plan.getEndDate() != null ? plan.getEndDate() : existingPlan.getEndDate();
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        
        // 关键修复：使用LambdaUpdateWrapper替代updateById，解决NOT_NULL策略导致null字段无法清空的问题
        // updateById默认使用NOT_NULL策略，会忽略所有null字段，导致以下问题：
        // 1. 用户清空targetDeptIds/targetPositionIds/targetUserIds时，旧值无法被清除
        // 2. 用户将needExam从1改为0时，paperId无法被清除
        // 3. 目标类型切换后，旧目标数据残留，影响发布校验
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TrainingPlan> updateWrapper = 
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(TrainingPlan::getId, plan.getId());
        
        // 设置所有字段（包括null值），确保前端传null时能正确清空数据库中的旧值
        updateWrapper.set(TrainingPlan::getPlanName, plan.getPlanName());
        updateWrapper.set(TrainingPlan::getPlanCode, plan.getPlanCode());
        updateWrapper.set(TrainingPlan::getPlanType, plan.getPlanType());
        updateWrapper.set(TrainingPlan::getCourseId, plan.getCourseId());
        updateWrapper.set(TrainingPlan::getStartDate, plan.getStartDate());
        updateWrapper.set(TrainingPlan::getEndDate, plan.getEndDate());
        updateWrapper.set(TrainingPlan::getTargetType, plan.getTargetType());
        // 关键：targetDeptIds/targetPositionIds/targetUserIds可以为null，用于清空旧值
        updateWrapper.set(TrainingPlan::getTargetDeptIds, plan.getTargetDeptIds());
        updateWrapper.set(TrainingPlan::getTargetPositionIds, plan.getTargetPositionIds());
        updateWrapper.set(TrainingPlan::getTargetUserIds, plan.getTargetUserIds());
        updateWrapper.set(TrainingPlan::getNeedExam, plan.getNeedExam());
        // 关键：当needExam=0时，paperId应该被清空为null
        updateWrapper.set(TrainingPlan::getPaperId, plan.getPaperId());
        updateWrapper.set(TrainingPlan::getPassScore, plan.getPassScore());
        updateWrapper.set(TrainingPlan::getMaxRetake, plan.getMaxRetake());
        updateWrapper.set(TrainingPlan::getMinStudyTime, plan.getMinStudyTime());
        updateWrapper.set(TrainingPlan::getMinProgress, plan.getMinProgress());
        updateWrapper.set(TrainingPlan::getPlanDesc, plan.getPlanDesc());
        updateWrapper.set(TrainingPlan::getPlanObjective, plan.getPlanObjective());
        // 状态变更必须通过专用的publish/end/archive接口完成，不允许通过更新接口修改
        // 不设置status字段，保持数据库原值
        
        return baseMapper.update(null, updateWrapper) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlan(Long id) {
        // 获取培训计划
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 只有草稿状态才能删除
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的培训计划才能删除");
        }
        
        // 检查培训计划是否有用户参与记录
        Long count = userTrainingPlanMapper.selectCount(
            new LambdaQueryWrapper<UserTrainingPlan>().eq(UserTrainingPlan::getPlanId, id)
        );
        if (count > 0) {
            throw new BusinessException("培训计划存在学习记录，无法删除");
        }
        
        // 删除培训计划时清理PlanCourse关联数据
        planCourseMapper.delete(
            new LambdaQueryWrapper<PlanCourse>().eq(PlanCourse::getPlanId, id)
        );
        
        return baseMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean hasParticipants(Long planId) {
        Long count = userTrainingPlanMapper.selectCount(
            new LambdaQueryWrapper<UserTrainingPlan>().eq(UserTrainingPlan::getPlanId, planId)
        );
        return count > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从草稿(0)状态发布
        if (existingPlan.getStatus() != 0) {
            throw new BusinessException("当前状态不允许发布，只有草稿状态可以发布");
        }
        
        // 验证必要字段：培训名称
        if (existingPlan.getPlanName() == null || existingPlan.getPlanName().trim().isEmpty()) {
            throw new BusinessException("请先设置培训计划名称");
        }
        
        // 验证必要字段：日期
        if (existingPlan.getStartDate() == null || existingPlan.getEndDate() == null) {
            throw new BusinessException("请先设置培训计划的开始日期和结束日期");
        }
        
        // 验证日期合理性
        if (existingPlan.getStartDate().isAfter(existingPlan.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        
        // 修复：移除开始日期不能早于当前日期的限制，允许发布过去日期的培训计划（可能用于补录等场景）
        
        // 验证必要字段：培训类型
        if (existingPlan.getPlanType() == null) {
            throw new BusinessException("请先设置培训类型");
        }
        
        // 验证必要字段：目标类型
        if (existingPlan.getTargetType() == null) {
            throw new BusinessException("请先设置目标类型");
        }
        
        // 根据目标类型验证不同的目标字段
        Integer targetType = existingPlan.getTargetType();
        if (targetType == 1) {
            // 按部门：验证目标部门
            if (existingPlan.getTargetDeptIds() == null || existingPlan.getTargetDeptIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标部门");
            }
            // 验证JSON数组不为空
            if (isJsonArrayEmpty(existingPlan.getTargetDeptIds())) {
                throw new BusinessException("请至少选择一个目标部门");
            }
        } else if (targetType == 2) {
            // 按岗位：验证目标岗位
            if (existingPlan.getTargetPositionIds() == null || existingPlan.getTargetPositionIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标岗位");
            }
            if (isJsonArrayEmpty(existingPlan.getTargetPositionIds())) {
                throw new BusinessException("请至少选择一个目标岗位");
            }
        } else if (targetType == 3) {
            // 按人员：验证目标人员
            if (existingPlan.getTargetUserIds() == null || existingPlan.getTargetUserIds().trim().isEmpty()) {
                throw new BusinessException("请先设置目标人员");
            }
            if (isJsonArrayEmpty(existingPlan.getTargetUserIds())) {
                throw new BusinessException("请至少选择一个目标人员");
            }
        }
        
        // 验证必要字段：关联课程
        if (existingPlan.getCourseId() == null) {
            throw new BusinessException("请先关联培训课程");
        }
        
        // 修复：验证考试相关字段，当needExam=1时必须关联试卷
        if (existingPlan.getNeedExam() != null && existingPlan.getNeedExam() == 1) {
            if (existingPlan.getPaperId() == null) {
                throw new BusinessException("已设置需要考试，请先关联试卷");
            }
            // 验证关联试卷是否存在且已发布
            Paper paperObj = paperMapper.selectById(existingPlan.getPaperId());
            if (paperObj == null) {
                throw new BusinessException("关联的试卷不存在，请重新选择试卷");
            }
            if (paperObj.getStatus() == null || paperObj.getStatus() != 1) {
                throw new BusinessException("关联的试卷尚未发布，请先发布试卷或重新选择已发布的试卷");
            }
        }
        
        // 验证关联课程是否存在且可用
        Course course = courseMapper.selectById(existingPlan.getCourseId());
        if (course == null) {
            throw new BusinessException("关联的课程不存在");
        }
        // 课程状态定义：0草稿 1待审核 2已上架 3已下架 4审核驳回
        // 发布培训计划时，自动将未上架的课程上架
        if (course.getStatus() != 2) {
            if (course.getStatus() == 0 || course.getStatus() == 1 || course.getStatus() == 3 || course.getStatus() == 4) {
                // 课程处于草稿、待审核、已下架或审核驳回状态，自动审核通过并上架
                Course courseUpdate = new Course();
                courseUpdate.setId(course.getId());
                courseUpdate.setStatus(2); // 已上架
                courseUpdate.setAuditBy(getCurrentUserId());
                courseUpdate.setAuditTime(LocalDateTime.now());
                courseUpdate.setAuditRemark("发布培训计划时自动审核通过");
                courseMapper.updateById(courseUpdate);
                log.info("发布培训计划时自动审核通过课程[{}]", course.getId());
            } else {
                throw new BusinessException("关联的课程状态异常(状态码:" + course.getStatus() + ")，无法发布培训计划");
            }
        }
        
        // 修复：使用乐观锁防止并发重复发布
        int updateCount = baseMapper.updateStatusWithOptimisticLock(id, 0, 1);
        if (updateCount == 0) {
            throw new BusinessException("发布失败，培训计划状态已被修改，请刷新后重试");
        }
        
        // 修复：发布培训计划时，为目标用户创建UserPlan记录，使其能在"我的课程"中看到培训计划
        try {
            List<Long> targetUserIds = findTargetUserIds(existingPlan);
            if (!targetUserIds.isEmpty()) {
                createUserPlansForTargetUsers(existingPlan, targetUserIds);
            }
        } catch (Exception e) {
            // 记录日志但不影响发布流程
            log.error("为培训计划[{}]创建用户学习记录失败: {}", id, e.getMessage(), e);
        }
        
        return true;
    }
    
    /**
     * 根据目标类型查找目标用户ID列表
     */
    private List<Long> findTargetUserIds(TrainingPlan plan) {
        Integer targetType = plan.getTargetType();
        if (targetType == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, 1); // 只查询正常状态的用户
        
        switch (targetType) {
            case 1: // 按部门
                String deptIds = plan.getTargetDeptIds();
                if (StringUtils.hasText(deptIds) && !isJsonArrayEmpty(deptIds)) {
                    List<Long> deptIdList = JsonArrayUtils.parseJsonArrayToLongList(deptIds);
                    if (!deptIdList.isEmpty()) {
                        wrapper.in(User::getDeptId, deptIdList);
                        return userMapper.selectList(wrapper).stream()
                                .map(User::getId)
                                .collect(Collectors.toList());
                    }
                }
                break;
                
            case 2: // 按岗位
                String positionIds = plan.getTargetPositionIds();
                if (StringUtils.hasText(positionIds) && !isJsonArrayEmpty(positionIds)) {
                    List<Long> positionIdList = JsonArrayUtils.parseJsonArrayToLongList(positionIds);
                    if (!positionIdList.isEmpty()) {
                        wrapper.in(User::getPositionId, positionIdList);
                        return userMapper.selectList(wrapper).stream()
                                .map(User::getId)
                                .collect(Collectors.toList());
                    }
                }
                break;
                
            case 3: // 按个人
                String userIds = plan.getTargetUserIds();
                if (StringUtils.hasText(userIds) && !isJsonArrayEmpty(userIds)) {
                    return JsonArrayUtils.parseJsonArrayToLongList(userIds);
                }
                break;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 为目标用户创建培训计划关联记录
     * 修复：使用UserTrainingPlan实体（映射到etms_user_plan表）替代UserPlan（错误映射到learning_progress表）
     */
    private void createUserPlansForTargetUsers(TrainingPlan plan, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        Long planId = plan.getId();
        
        // 查询已存在的UserTrainingPlan记录，避免重复创建
        List<UserTrainingPlan> existingUserPlans = userTrainingPlanMapper.selectList(
            new LambdaQueryWrapper<UserTrainingPlan>().eq(UserTrainingPlan::getPlanId, planId)
        );
        Set<Long> existingUserIds = existingUserPlans.stream()
                .map(UserTrainingPlan::getUserId)
                .collect(Collectors.toSet());
        
        // 为新用户创建UserTrainingPlan记录
        int successCount = 0;
        int failCount = 0;
        for (Long userId : userIds) {
            if (!existingUserIds.contains(userId)) {
                try {
                    UserTrainingPlan userTrainingPlan = new UserTrainingPlan();
                    userTrainingPlan.setUserId(userId);
                    userTrainingPlan.setPlanId(planId);
                    userTrainingPlan.setStatus(0); // 未开始
                    userTrainingPlanMapper.insert(userTrainingPlan);
                    successCount++;
                } catch (Exception e) {
                    // 捕获单条插入异常（如唯一约束冲突），不影响其他用户
                    failCount++;
                    log.warn("为用户[{}]创建培训计划[{}]关联记录失败: {}", userId, planId, e.getMessage());
                }
            }
        }
        
        if (successCount > 0) {
            log.info("为培训计划[{}]成功创建{}条用户关联记录", planId, successCount);
        }
        if (failCount > 0) {
            log.warn("为培训计划[{}]创建用户关联记录时{}条失败", planId, failCount);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean archivePlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从已结束(3)状态归档
        if (existingPlan.getStatus() != 3) {
            throw new BusinessException("当前状态不允许归档，只有已结束状态可以归档");
        }
        
        // 修复：使用乐观锁防止并发重复归档
        int updateCount = baseMapper.updateStatusWithOptimisticLock(id, 3, 4);
        if (updateCount == 0) {
            throw new BusinessException("归档失败，培训计划状态已被修改，请刷新后重试");
        }
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endPlan(Long id) {
        // 获取当前培训计划状态
        TrainingPlan existingPlan = baseMapper.selectById(id);
        if (existingPlan == null) {
            throw new BusinessException("培训计划不存在");
        }
        
        // 校验状态流转：只能从已发布(1)或进行中(2)状态结束
        if (existingPlan.getStatus() != 1 && existingPlan.getStatus() != 2) {
            throw new BusinessException("当前状态不允许结束，只有已发布或进行中状态可以结束");
        }
        
        // 修复：使用乐观锁防止并发重复结束
        // 已发布(1)或进行中(2)状态可以结束
        int updateCount = 0;
        if (existingPlan.getStatus() == 1) {
            updateCount = baseMapper.updateStatusWithOptimisticLock(id, 1, 3);
        } else if (existingPlan.getStatus() == 2) {
            updateCount = baseMapper.updateStatusWithOptimisticLock(id, 2, 3);
        }
        if (updateCount == 0) {
            throw new BusinessException("结束失败，培训计划状态已被修改，请刷新后重试");
        }
        return true;
    }
    
    private String getPlanTypeName(Integer planType) {
        if (planType == null) return "未知";
        return planType == 1 ? "必修" : "选修";
    }
    
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已发布";
            case 2: return "进行中";
            case 3: return "已结束";
            case 4: return "已归档";
            default: return "未知";
        }
    }
    
    /**
     * 检查ID是否在JSON数组字符串中
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 ["1","2","3"]
     * @param targetId 要查找的ID
     * @return 是否包含该ID
     */
    private boolean isIdInJsonArray(String jsonArrayStr, Long targetId) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 修复：使用静态常量OBJECT_MAPPER解析JSON数组，避免每次创建新实例
            java.util.List<?> list = OBJECT_MAPPER.readValue(jsonArrayStr, java.util.List.class);
            
            for (Object item : list) {
                if (item == null) continue;
                // 支持数字类型和字符串类型
                Long itemId = null;
                if (item instanceof Number) {
                    itemId = ((Number) item).longValue();
                } else if (item instanceof String) {
                    try {
                        itemId = Long.parseLong((String) item);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
                if (itemId != null && itemId.equals(targetId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // JSON解析失败时回退到简单字符串匹配
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
    }
    
    /**
     * 检查JSON数组字符串是否为空
     * @param jsonArrayStr JSON数组字符串，如 "[1, 2, 3]" 或 "[]"
     * @return 是否为空数组
     */
    private boolean isJsonArrayEmpty(String jsonArrayStr) {
        if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
            return true;
        }
        
        String str = jsonArrayStr.trim();
        // 处理JSON数组格式
        if (str.equals("[]")) {
            return true;
        }
        
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1).trim();
            // 移除所有空白后检查是否为空
            if (str.isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        User user = userMapper.selectByUsername(username);
        return user != null ? user.getId() : null;
    }
}
