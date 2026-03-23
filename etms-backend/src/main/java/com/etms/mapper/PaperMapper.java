package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 试卷Mapper接口
 */
@Mapper
public interface PaperMapper extends BaseMapper<Paper> {
    
    /**
     * 使用乐观锁方式发布试卷
     * 修复并发问题：只有当当前状态为草稿(0)时才更新为已发布(1)
     * @param id 试卷ID
     * @param expectedStatus 期望的当前状态
     * @param newStatus 新状态
     * @return 影响的行数（0表示更新失败，状态已被其他线程修改）
     */
    @Update("UPDATE exam_paper SET status = #{newStatus}, update_time = NOW() WHERE id = #{id} AND status = #{expectedStatus}")
    int updateStatusWithOptimisticLock(@Param("id") Long id, @Param("expectedStatus") Integer expectedStatus, @Param("newStatus") Integer newStatus);
}
