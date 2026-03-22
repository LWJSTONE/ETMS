package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.DictType;
import com.etms.entity.DictData;
import com.etms.exception.BusinessException;
import com.etms.mapper.DictTypeMapper;
import com.etms.mapper.DictDataMapper;
import com.etms.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典服务实现类
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictService {
    
    private final DictDataMapper dictDataMapper;
    
    @Override
    public Page<DictType> pageDictTypes(Page<DictType> page, String dictName, String dictType, Integer status) {
        LambdaQueryWrapper<DictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictName), DictType::getDictName, dictName)
               .like(StringUtils.hasText(dictType), DictType::getDictType, dictType)
               .eq(status != null, DictType::getStatus, status)
               .orderByDesc(DictType::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
    
    @Override
    public DictType getDictTypeDetail(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public void addDictType(DictType dictType) {
        // 检查字典类型是否重复
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<DictType>().eq(DictType::getDictType, dictType.getDictType())
        );
        if (count > 0) {
            throw new BusinessException("字典类型已存在");
        }
        
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        baseMapper.insert(dictType);
    }
    
    @Override
    public void updateDictType(DictType dictType) {
        // 检查字典类型是否重复（排除自身）
        Long count = baseMapper.selectCount(
            new LambdaQueryWrapper<DictType>()
                .eq(DictType::getDictType, dictType.getDictType())
                .ne(DictType::getId, dictType.getId())
        );
        if (count > 0) {
            throw new BusinessException("字典类型已存在");
        }
        baseMapper.updateById(dictType);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        // 删除字典类型下的所有数据
        dictDataMapper.delete(
            new LambdaQueryWrapper<DictData>().eq(DictData::getDictTypeId, id)
        );
        // 删除字典类型
        baseMapper.deleteById(id);
    }
    
    @Override
    public List<DictData> getDictDataList(Long dictTypeId) {
        return dictDataMapper.selectList(
            new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictTypeId, dictTypeId)
                .orderByAsc(DictData::getDictSort)
        );
    }
    
    @Override
    public void addDictData(DictData dictData) {
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getDictSort() == null) {
            dictData.setDictSort(0);
        }
        dictDataMapper.insert(dictData);
    }
    
    @Override
    public void updateDictData(DictData dictData) {
        dictDataMapper.updateById(dictData);
    }
    
    @Override
    public void deleteDictData(Long id) {
        dictDataMapper.deleteById(id);
    }
    
    @Override
    public List<DictData> getDictDataByType(String dictType) {
        // 先获取字典类型
        DictType type = baseMapper.selectOne(
            new LambdaQueryWrapper<DictType>().eq(DictType::getDictType, dictType)
        );
        if (type == null) {
            return List.of();
        }
        return getDictDataList(type.getId());
    }
}
