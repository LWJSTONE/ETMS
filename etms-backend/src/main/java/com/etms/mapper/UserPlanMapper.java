package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.UserPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户培训计划学习记录Mapper
 */
@Mapper
public interface UserPlanMapper extends BaseMapper<UserPlan> {
}
