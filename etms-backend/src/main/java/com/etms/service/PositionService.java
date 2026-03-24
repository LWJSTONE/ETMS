package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Position;
import javax.servlet.http.HttpServletResponse;

/**
 * 岗位服务接口
 */
public interface PositionService extends IService<Position> {
    
    /**
     * 分页查询岗位列表
     */
    Page<Position> pagePositions(Page<Position> page, String positionName, String positionCode, Integer status);
    
    /**
     * 获取岗位详情
     */
    Position getPositionDetail(Long id);
    
    /**
     * 新增岗位
     */
    void addPosition(Position position);
    
    /**
     * 更新岗位
     */
    void updatePosition(Position position);
    
    /**
     * 删除岗位
     */
    void deletePosition(Long id);
    
    /**
     * 检查岗位是否有用户关联
     * @param positionId 岗位ID
     * @return 是否有用户关联
     */
    boolean hasUsers(Long positionId);

    /**
     * 导出岗位
     */
    void exportPositions(String positionName, String positionCode, Integer status, HttpServletResponse response);
}
