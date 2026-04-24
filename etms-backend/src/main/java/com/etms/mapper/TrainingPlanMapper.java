package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 培训计划Mapper接口
 */
@Mapper
public interface TrainingPlanMapper extends BaseMapper<TrainingPlan> {
    
    /**
     * 使用乐观锁方式发布培训计划
     * 修复并发问题：只有当当前状态为草稿(0)时才更新为已发布(1)
     * @param id 培训计划ID
     * @param expectedStatus 期望的当前状态
     * @param newStatus 新状态
     * @return 影响的行数（0表示更新失败，状态已被其他线程修改）
     */
    @Update("UPDATE training_plan SET status = #{newStatus}, update_time = NOW() WHERE id = #{id} AND status = #{expectedStatus} AND deleted = 0")
    int updateStatusWithOptimisticLock(@Param("id") Long id, @Param("expectedStatus") Integer expectedStatus, @Param("newStatus") Integer newStatus);

    /**
     * 检查计划编码是否已存在（包含软删除记录）
     * 修复：@TableLogic的selectCount只查deleted=0的记录，但数据库UNIQUE KEY约束对全部记录生效，
     * 导致软删除记录的planCode冲突时selectCount返回0但INSERT抛DuplicateKeyException，
     * 在@Transactional内catch后抛BusinessException可能触发UnexpectedRollbackException
     * @param planCode 计划编码
     * @return 包含软删除记录的数量
     */
    @Select("SELECT COUNT(*) FROM training_plan WHERE plan_code = #{planCode}")
    Long countByPlanCodeIncludingDeleted(@Param("planCode") String planCode);
}
