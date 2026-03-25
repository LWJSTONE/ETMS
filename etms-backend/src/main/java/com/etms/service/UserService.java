package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.dto.LoginDTO;
import com.etms.dto.UserDTO;
import com.etms.entity.User;
import com.etms.vo.LoginVO;
import com.etms.vo.UserVO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO, HttpServletRequest request);
    
    /**
     * 用户登出
     */
    void logout(HttpServletRequest request);
    
    /**
     * 分页查询用户列表
     */
    Page<UserVO> pageUsers(Page<User> page, UserDTO userDTO);
    
    /**
     * 获取用户详情
     */
    UserVO getUserDetail(Long id);
    
    /**
     * 新增用户
     */
    boolean addUser(UserDTO userDTO);
    
    /**
     * 更新用户
     */
    boolean updateUser(UserDTO userDTO);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long id);
    
    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     * @param userId 用户ID
     * @return 新生成的密码（调用方应通过安全渠道通知用户）
     */
    String resetPassword(Long userId);
    
    /**
     * 修改状态
     */
    boolean updateStatus(Long userId, Integer status);
    
    /**
     * 分配角色
     */
    boolean assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 获取当前登录用户
     */
    User getCurrentUser();

    /**
     * 导出用户
     */
    void exportUsers(UserDTO userDTO, HttpServletResponse response);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);
}
