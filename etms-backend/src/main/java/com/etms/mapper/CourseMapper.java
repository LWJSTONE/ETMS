package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
