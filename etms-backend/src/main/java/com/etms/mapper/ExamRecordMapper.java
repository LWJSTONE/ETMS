package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 考试记录Mapper
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {
    
    /**
     * 使用乐观锁方式更新考试状态
     * 修复并发问题：只有当当前状态为期望状态时才更新
     * @param recordId 考试记录ID
     * @param expectedStatus 期望的当前状态
     * @param newStatus 新状态
     * @return 影响的行数（0表示更新失败，状态已被其他线程修改）
     */
    @Update("UPDATE exam_record SET status = #{newStatus} WHERE id = #{recordId} AND status = #{expectedStatus}")
    int updateStatusToSubmitted(@Param("recordId") Long recordId, @Param("expectedStatus") Integer expectedStatus, @Param("newStatus") Integer newStatus);
    
    /**
     * 使用SQL聚合函数计算平均分，避免内存查询
     * 性能优化：使用数据库AVG函数替代内存计算
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 平均分
     */
    @Select("<script>" +
            "SELECT COALESCE(AVG(user_score), 0) FROM exam_record " +
            "WHERE status IN (1, 2, 3, 4) " +
            "<if test='startTime != null'> AND submit_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND submit_time &lt;= #{endTime} </if>" +
            "</script>")
    Double calculateAvgScore(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
