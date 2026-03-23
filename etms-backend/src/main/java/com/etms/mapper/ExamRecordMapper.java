package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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
    @Update("UPDATE exam_record SET status = #{newStatus}, update_time = NOW() WHERE id = #{recordId} AND status = #{expectedStatus}")
    int updateStatusToSubmitted(@Param("recordId") Long recordId, @Param("expectedStatus") Integer expectedStatus, @Param("newStatus") Integer newStatus);
}
