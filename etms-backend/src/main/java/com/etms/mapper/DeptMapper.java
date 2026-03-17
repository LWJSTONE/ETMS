package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
    
    /**
     * 查询部门树形结构
     */
    List<Dept> selectDeptTree();
    
    /**
     * 根据用户ID查询部门
     */
    Dept selectByUserId(Long userId);
}
