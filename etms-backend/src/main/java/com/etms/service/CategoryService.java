package com.etms.service;

import com.etms.entity.Category;
import com.etms.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {
    
    /**
     * 获取分类树形结构
     */
    List<CategoryVO> getCategoryTree(Integer categoryType);
    
    /**
     * 获取分类列表
     */
    List<CategoryVO> getCategoryList(Integer categoryType, Long parentId);
    
    /**
     * 获取分类详情
     */
    CategoryVO getCategoryDetail(Long id);
    
    /**
     * 新增分类
     */
    void addCategory(Category category);
    
    /**
     * 更新分类
     */
    void updateCategory(Category category);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    
    /**
     * 检查分类是否有子分类
     * @param categoryId 分类ID
     * @return 是否有子分类
     */
    boolean hasChildren(Long categoryId);
    
    /**
     * 检查分类是否有关联课程
     * @param categoryId 分类ID
     * @return 是否有关联课程
     */
    boolean hasCourses(Long categoryId);
    
    /**
     * 更新分类状态
     */
    void updateStatus(Long id, Integer status);
}
