package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.Position;
import com.etms.exception.BusinessException;
import com.etms.mapper.PositionMapper;
import com.etms.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 岗位服务实现类
 */
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {
    
    @Override
    public Page<Position> pagePositions(Page<Position> page, String positionName, String positionCode, Integer status) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(positionName), Position::getPositionName, positionName)
               .like(StringUtils.hasText(positionCode), Position::getPositionCode, positionCode)
               .eq(status != null, Position::getStatus, status)
               .orderByAsc(Position::getSortOrder)
               .orderByDesc(Position::getCreateTime);
        
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Position getPositionDetail(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public void addPosition(Position position) {
        // 检查岗位编码是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Position>().eq(Position::getPositionCode, position.getPositionCode())
        );
        if (count > 0) {
            throw new BusinessException("岗位编码已存在");
        }
        
        if (position.getStatus() == null) {
            position.setStatus(1);
        }
        if (position.getSortOrder() == null) {
            position.setSortOrder(0);
        }
        
        baseMapper.insert(position);
    }
    
    @Override
    public void updatePosition(Position position) {
        // 检查岗位编码是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<Position>()
                .eq(Position::getPositionCode, position.getPositionCode())
                .ne(Position::getId, position.getId())
        );
        if (count > 0) {
            throw new BusinessException("岗位编码已存在");
        }
        
        baseMapper.updateById(position);
    }
    
    @Override
    public void deletePosition(Long id) {
        // 检查是否有用户关联此岗位
        // 可以根据实际业务添加检查逻辑
        
        baseMapper.deleteById(id);
    }
}
