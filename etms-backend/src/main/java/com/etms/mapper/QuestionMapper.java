package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 题库Mapper接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    /**
     * 随机抽取题目（使用参数化查询避免SQL注入）
     * @param questionType 题目类型
     * @param difficulty 难度
     * @param count 数量
     * @param courseId 课程ID
     * @return 题目列表
     */
    @Select("<script>" +
            "SELECT * FROM exam_question " +
            "WHERE status = 1 " +
            "<if test='questionType != null'> AND question_type = #{questionType} </if>" +
            "<if test='difficulty != null'> AND difficulty = #{difficulty} </if>" +
            "<if test='courseId != null'> AND course_id = #{courseId} </if>" +
            "ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Question> selectRandomQuestions(@Param("questionType") Integer questionType, 
                                          @Param("difficulty") Integer difficulty, 
                                          @Param("count") Integer count, 
                                          @Param("courseId") Long courseId);
    
    /**
     * 统计题目被已发布试卷使用的次数
     * @param questionId 题目ID
     * @return 使用次数
     */
    @Select("SELECT COUNT(DISTINCT pq.paper_id) FROM exam_paper_question pq " +
            "INNER JOIN exam_paper p ON pq.paper_id = p.id " +
            "WHERE pq.question_id = #{questionId} AND p.status = 1")
    Long countUsedInPublishedPapers(@Param("questionId") Long questionId);
}
