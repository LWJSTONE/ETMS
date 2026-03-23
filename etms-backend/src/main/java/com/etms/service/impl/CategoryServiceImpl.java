package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Category;
import com.etms.exception.BusinessException;
import com.etms.mapper.CategoryMapper;
import com.etms.mapper.CourseMapper;
import com.etms.mapper.QuestionMapper;
import com.etms.service.CategoryService;
import com.etms.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    
    @Override
    public List<CategoryVO> getCategoryTree(Integer categoryType) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryType != null, Category::getCategoryType, categoryType)
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getCreateTime);
        
        List<Category> categories = baseMapper.selectList(wrapper);
        
        // 转换为VO
        List<CategoryVO> voList = categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(voList, 0L);
    }
    
    @Override
    public List<CategoryVO> getCategoryList(Integer categoryType, Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(categoryType != null, Category::getCategoryType, categoryType)
               .eq(parentId != null, Category::getParentId, parentId)
               .orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getCreateTime);
        
        List<Category> categories = baseMapper.selectList(wrapper);
        
        return categories.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public CategoryVO getCategoryDetail(Long id) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            return null;
        }
        
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        
        // 获取父分类名称
        if (category.getParentId() != null && category.getParentId() > 0) {
            Category parent = baseMapper.selectById(category.getParentId());
            if (parent != null) {
                vo.setParentName(parent.getCategoryName());
            }
        }
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCategory(Category category) {
        // 检查分类名称是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, category.getCategoryName())
                .eq(Category::getCategoryType, category.getCategoryType())
        );
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 检查分类编码是否重复
        if (StringUtils.hasText(category.getCategoryCode())) {
            count = baseMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                    .eq(Category::getCategoryCode, category.getCategoryCode())
            );
            if (count > 0) {
                throw new BusinessException("分类编码已存在");
            }
        }
        
        // 设置层级
        if (category.getParentId() == null || category.getParentId() <= 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = baseMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }
        
        // 设置排序号
        if (category.getSortOrder() == null) {
            Integer maxSort = baseMapper.selectMaxSortOrder(category.getParentId(), category.getCategoryType());
            category.setSortOrder(maxSort == null ? 1 : maxSort + 1);
        }
        
        // 设置默认状态
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        // 设置默认分类类型（1课程分类）
        if (category.getCategoryType() == null) {
            category.setCategoryType(1);
        }
        
        baseMapper.insert(category);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Category category) {
        Category existing = baseMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类名称是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getCategoryName, category.getCategoryName())
                .eq(Category::getCategoryType, existing.getCategoryType())
                .ne(Category::getId, category.getId())
        );
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        
        // 检查分类编码是否重复（排除自身）
        if (StringUtils.hasText(category.getCategoryCode())) {
            count = baseMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                    .eq(Category::getCategoryCode, category.getCategoryCode())
                    .ne(Category::getId, category.getId())
            );
            if (count > 0) {
                throw new BusinessException("分类编码已存在");
            }
        }
        
        // 检查父分类是否发生变化
        boolean parentChanged = false;
        Integer newLevel = existing.getLevel();
        
        // 不能将分类移动到自己的子分类下
        if (category.getParentId() != null && !category.getParentId().equals(existing.getParentId())) {
            parentChanged = true;
            
            if (isChildCategory(category.getId(), category.getParentId())) {
                throw new BusinessException("不能将分类移动到自己的子分类下");
            }
            
            // 更新层级
            if (category.getParentId() <= 0) {
                category.setParentId(0L);
                newLevel = 1;
            } else {
                Category parent = baseMapper.selectById(category.getParentId());
                if (parent == null) {
                    throw new BusinessException("父分类不存在");
                }
                newLevel = parent.getLevel() + 1;
            }
            category.setLevel(newLevel);
        }
        
        baseMapper.updateById(category);
        
        // 如果父分类发生变化，递归更新所有子分类的层级
        if (parentChanged) {
            updateChildrenLevel(category.getId(), newLevel);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Category category = baseMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有子分类
        Long childCount = baseMapper.selectCount(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException("存在子分类，无法删除");
        }
        
        // 检查是否有关联的课程
        if (category.getCategoryType() == 1) {
            Long courseCount = courseMapper.selectCount(
                new LambdaQueryWrapper<com.etms.entity.Course>()
                    .eq(com.etms.entity.Course::getCategoryId, id)
            );
            if (courseCount > 0) {
                throw new BusinessException("该分类下存在课程，无法删除");
            }
        }
        
        // 检查是否有关联的题目
        if (category.getCategoryType() == 2) {
            Long questionCount = questionMapper.selectCount(
                new LambdaQueryWrapper<com.etms.entity.Question>()
                    .eq(com.etms.entity.Question::getCategoryId, id)
            );
            if (questionCount > 0) {
                throw new BusinessException("该分类下存在题目，无法删除");
            }
        }
        
        baseMapper.deleteById(id);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        // 验证分类是否存在
        Category existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 验证状态值有效性（0:禁用, 1:启用）
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效，状态只能为0(禁用)或1(启用)");
        }
        
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        baseMapper.updateById(category);
    }
    
    /**
     * 构建树形结构
     */
    private List<CategoryVO> buildTree(List<CategoryVO> categories, Long parentId) {
        List<CategoryVO> tree = new ArrayList<>();
        
        Map<Long, List<CategoryVO>> groupMap = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getParentId() == null ? 0L : c.getParentId()));
        
        for (CategoryVO category : categories) {
            Long pid = category.getParentId() == null ? 0L : category.getParentId();
            if (pid.equals(parentId)) {
                category.setChildren(groupMap.get(category.getId()));
                tree.add(category);
            }
        }
        
        return tree;
    }
    
    /**
     * 判断是否为子分类
     */
    private boolean isChildCategory(Long parentId, Long childId) {
        if (parentId.equals(childId)) {
            return true;
        }
        
        List<Category> children = baseMapper.selectList(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, parentId)
        );
        
        for (Category child : children) {
            if (isChildCategory(child.getId(), childId)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 递归更新子分类的层级
     * @param parentId 父分类ID
     * @param parentLevel 父分类层级
     */
    private void updateChildrenLevel(Long parentId, Integer parentLevel) {
        // 查询所有直接子分类
        List<Category> children = baseMapper.selectList(
            new LambdaQueryWrapper<Category>().eq(Category::getParentId, parentId)
        );
        
        for (Category child : children) {
            // 更新子分类的层级
            child.setLevel(parentLevel + 1);
            baseMapper.updateById(child);
            
            // 递归更新子分类的子分类
            updateChildrenLevel(child.getId(), child.getLevel());
        }
    }
}
