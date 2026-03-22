package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷题目关联Mapper
 */
@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {
}
