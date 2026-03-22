package com.etms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.etms.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类Mapper接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    
    /**
     * 根据父ID查询子分类
     */
    List<Category> selectByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据分类类型查询分类列表
     */
    List<Category> selectByCategoryType(@Param("categoryType") Integer categoryType);
    
    /**
     * 查询最大排序号
     */
    Integer selectMaxSortOrder(@Param("parentId") Long parentId, @Param("categoryType") Integer categoryType);
}
