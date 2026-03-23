package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程Mapper接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    
    /**
     * 原子性地增加浏览次数
     * 修复并发问题：使用数据库原子操作
     * @param courseId 课程ID
     * @return 影响的行数
     */
    @Update("UPDATE training_course SET view_count = IFNULL(view_count, 0) + 1, update_time = NOW() WHERE id = #{courseId}")
    int incrementViewCount(@Param("courseId") Long courseId);
}
