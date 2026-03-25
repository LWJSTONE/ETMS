package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Dept;
import java.util.List;

/**
 * 部门服务接口
 */
public interface DeptService extends IService<Dept> {
    
    /**
     * 获取部门树形结构
     */
    List<Dept> getDeptTree();
    
    /**
     * 获取部门列表
     */
    List<Dept> getDeptList(Long parentId, String deptName, Integer status);
    
    /**
     * 新增部门
     */
    void addDept(Dept dept);
    
    /**
     * 更新部门
     */
    void updateDept(Dept dept);
    
    /**
     * 删除部门
     */
    void deleteDept(Long id);
    
    /**
     * 检查部门是否有用户
     * @param deptId 部门ID
     * @return 是否有用户
     */
    boolean hasUsers(Long deptId);
    
    /**
     * 检查部门是否有子部门
     * @param deptId 部门ID
     * @return 是否有子部门
     */
    boolean hasChildren(Long deptId);
}
