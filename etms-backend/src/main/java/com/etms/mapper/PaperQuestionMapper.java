package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 试卷题目关联Mapper
 */
@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {
    
    /**
     * 查询试卷题目的最大排序号
     * @param paperId 试卷ID
     * @return 最大排序号
     */
    @Select("SELECT MAX(sort_order) FROM exam_paper_question WHERE paper_id = #{paperId}")
    Integer selectMaxSortOrderByPaperId(Long paperId);
}
