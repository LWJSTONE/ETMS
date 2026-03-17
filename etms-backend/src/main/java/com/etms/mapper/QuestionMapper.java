package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库Mapper接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
