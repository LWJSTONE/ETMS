package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 签到记录Mapper接口
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
}
