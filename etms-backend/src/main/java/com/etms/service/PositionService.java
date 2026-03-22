package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.Position;

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
}
