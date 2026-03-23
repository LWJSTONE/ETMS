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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @Override
    public void exportPositions(String positionName, String positionCode, Integer status, HttpServletResponse response) {
        // 查询所有符合条件的岗位
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(positionName), Position::getPositionName, positionName)
               .like(StringUtils.hasText(positionCode), Position::getPositionCode, positionCode)
               .eq(status != null, Position::getStatus, status)
               .orderByAsc(Position::getSortOrder)
               .orderByDesc(Position::getCreateTime);
        
        List<Position> positions = baseMapper.selectList(wrapper);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("岗位数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        // 构建CSV格式的数据
        StringBuilder sb = new StringBuilder();
        sb.append("岗位名称,岗位编码,排序,状态,备注,创建时间\n");
        
        for (Position position : positions) {
            sb.append(position.getPositionName() != null ? position.getPositionName() : "").append(",")
              .append(position.getPositionCode() != null ? position.getPositionCode() : "").append(",")
              .append(position.getSortOrder() != null ? position.getSortOrder() : 0).append(",")
              .append(position.getStatus() != null && position.getStatus() == 1 ? "正常" : "禁用").append(",")
              .append(position.getRemark() != null ? position.getRemark() : "").append(",")
              .append(position.getCreateTime() != null ? position.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\n");
        }
        
        try {
            response.getWriter().write(sb.toString());
        } catch (IOException e) {
            throw new BusinessException("导出岗位数据失败：" + e.getMessage());
        }
    }
}
