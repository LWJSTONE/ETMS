package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.PlanCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 培训计划课程关联Mapper
 */
@Mapper
public interface PlanCourseMapper extends BaseMapper<PlanCourse> {
}
