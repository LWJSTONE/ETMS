package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.UserTrainingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户培训计划关联Mapper
 */
@Mapper
public interface UserTrainingPlanMapper extends BaseMapper<UserTrainingPlan> {

    /**
     * 根据培训计划ID查询关联的用户ID列表
     * @param planId 培训计划ID
     * @return 用户ID列表
     */
    @Select("SELECT user_id FROM etms_user_plan WHERE plan_id = #{planId}")
    List<Long> selectUserIdsByPlanId(@Param("planId") Long planId);

    /**
     * 根据用户ID查询关联的培训计划ID列表
     * @param userId 用户ID
     * @return 培训计划ID列表
     */
    @Select("SELECT plan_id FROM etms_user_plan WHERE user_id = #{userId}")
    List<Long> selectPlanIdsByUserId(@Param("userId") Long userId);
}
