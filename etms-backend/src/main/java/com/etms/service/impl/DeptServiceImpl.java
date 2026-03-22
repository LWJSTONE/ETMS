package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Dept;
import com.etms.entity.User;
import com.etms.mapper.DeptMapper;
import com.etms.mapper.UserMapper;
import com.etms.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 部门服务实现类
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {
    
    private final UserMapper userMapper;
    
    @Override
    public List<Dept> getDeptTree() {
        List<Dept> allDepts = baseMapper.selectList(
                new LambdaQueryWrapper<Dept>().orderByAsc(Dept::getSortOrder)
        );
        return buildDeptTree(allDepts, 0L);
    }
    
    @Override
    public List<Dept> getDeptList(Long parentId) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(parentId != null, Dept::getParentId, parentId)
               .orderByAsc(Dept::getSortOrder);
        return baseMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDept(Dept dept) {
        // 设置层级和祖级列表
        if (dept.getParentId() == null || dept.getParentId() == 0) {
            dept.setParentId(0L);
            dept.setLevel(1);
            dept.setAncestors("0");
        } else {
            Dept parentDept = baseMapper.selectById(dept.getParentId());
            // 添加父部门不存在的处理
            if (parentDept == null) {
                throw new RuntimeException("父部门不存在");
            }
            dept.setLevel(parentDept.getLevel() + 1);
            dept.setAncestors(parentDept.getAncestors() + "," + parentDept.getId());
        }
        baseMapper.insert(dept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(Dept dept) {
        baseMapper.updateById(dept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        // 检查是否有子部门
        Long childCount = baseMapper.selectCount(
                new LambdaQueryWrapper<Dept>().eq(Dept::getParentId, id)
        );
        if (childCount > 0) {
            throw new RuntimeException("存在子部门，无法删除");
        }
        
        // 检查部门下是否有用户
        Long userCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeptId, id)
        );
        if (userCount > 0) {
            throw new RuntimeException("部门下存在用户，无法删除");
        }
        
        baseMapper.deleteById(id);
    }
    
    /**
     * 构建部门树
     */
    private List<Dept> buildDeptTree(List<Dept> depts, Long parentId) {
        return depts.stream()
                .filter(dept -> parentId.equals(dept.getParentId()))
                .peek(dept -> {
                    List<Dept> children = buildDeptTree(depts, dept.getId());
                    dept.setChildren(children);
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
