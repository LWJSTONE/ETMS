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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;

/**
 * 字典服务实现类
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictService {
    
    private final DictDataMapper dictDataMapper;
    
    /** 字典缓存，key为字典类型，value为字典数据列表 */
    private final Map<String, List<DictData>> dictCache = new ConcurrentHashMap<>();
    
    /**
     * 初始化缓存
     */
    @PostConstruct
    public void initCache() {
        try {
            refreshCache();
        } catch (Exception e) {
            // 数据库未初始化时忽略错误，等数据库就绪后再初始化
        }
    }
    
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(DictType dictType) {
        // 检查字典类型是否存在
        DictType existing = baseMapper.selectById(dictType.getId());
        if (existing == null) {
            throw new BusinessException("字典类型不存在");
        }
        
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
        
        // 如果字典类型编码变更，需要更新缓存中的key
        if (!existing.getDictType().equals(dictType.getDictType())) {
            dictCache.remove(existing.getDictType());
            refreshCacheByDictTypeId(dictType.getId());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        // 获取字典类型信息
        DictType dictType = baseMapper.selectById(id);
        if (dictType == null) {
            throw new BusinessException("字典类型不存在");
        }
        
        // 修复：系统内置字典类型保护机制（以sys_开头的为系统内置）
        if (dictType.getDictType() != null && dictType.getDictType().startsWith("sys_")) {
            throw new BusinessException("系统内置字典类型不能删除");
        }
        
        // 删除字典类型下的所有数据
        dictDataMapper.delete(
            new LambdaQueryWrapper<DictData>().eq(DictData::getDictTypeId, id)
        );
        // 删除字典类型
        baseMapper.deleteById(id);
        
        // 清除缓存
        dictCache.remove(dictType.getDictType());
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
    @Transactional(rollbackFor = Exception.class)
    public void addDictData(DictData dictData) {
        // 修复：添加字典数据唯一性校验（同一字典类型下dictLabel不能重复）
        Long count = dictDataMapper.selectCount(
            new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictTypeId, dictData.getDictTypeId())
                .eq(DictData::getDictLabel, dictData.getDictLabel())
        );
        if (count > 0) {
            throw new BusinessException("该字典类型下已存在相同标签的数据");
        }
        
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getDictSort() == null) {
            dictData.setDictSort(0);
        }
        dictDataMapper.insert(dictData);
        
        // 修复：更新缓存
        refreshCacheByDictTypeId(dictData.getDictTypeId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(DictData dictData) {
        // 修复：添加唯一性校验（同一字典类型下dictLabel不能重复，排除自身）
        if (dictData.getDictLabel() != null && dictData.getDictTypeId() != null) {
            Long count = dictDataMapper.selectCount(
                new LambdaQueryWrapper<DictData>()
                    .eq(DictData::getDictTypeId, dictData.getDictTypeId())
                    .eq(DictData::getDictLabel, dictData.getDictLabel())
                    .ne(dictData.getId() != null, DictData::getId, dictData.getId())
            );
            if (count > 0) {
                throw new BusinessException("该字典类型下已存在相同标签的数据");
            }
        }
        
        dictDataMapper.updateById(dictData);
        
        // 修复：更新缓存
        refreshCacheByDictTypeId(dictData.getDictTypeId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        // 获取字典数据信息，用于更新缓存
        DictData dictData = dictDataMapper.selectById(id);
        if (dictData == null) {
            throw new BusinessException("字典数据不存在");
        }
        
        dictDataMapper.deleteById(id);
        
        // 修复：更新缓存
        refreshCacheByDictTypeId(dictData.getDictTypeId());
    }
    
    @Override
    public List<DictData> getDictDataByType(String dictType) {
        // 先从缓存获取
        List<DictData> cachedData = dictCache.get(dictType);
        if (cachedData != null) {
            return cachedData;
        }
        
        // 缓存中没有，从数据库查询
        DictType type = baseMapper.selectOne(
            new LambdaQueryWrapper<DictType>().eq(DictType::getDictType, dictType)
        );
        if (type == null) {
            return Collections.emptyList();
        }
        List<DictData> dataList = getDictDataList(type.getId());
        
        // 放入缓存
        dictCache.put(dictType, dataList);
        return dataList;
    }
    
    @Override
    public void refreshCache() {
        // 清空缓存
        dictCache.clear();
        
        // 查询所有字典类型
        List<DictType> dictTypes = baseMapper.selectList(
            new LambdaQueryWrapper<DictType>().eq(DictType::getStatus, 1)
        );
        
        // 遍历所有字典类型，将数据加载到缓存
        for (DictType dictType : dictTypes) {
            List<DictData> dataList = dictDataMapper.selectList(
                new LambdaQueryWrapper<DictData>()
                    .eq(DictData::getDictTypeId, dictType.getId())
                    .eq(DictData::getStatus, 1)
                    .orderByAsc(DictData::getDictSort)
            );
            dictCache.put(dictType.getDictType(), dataList);
        }
    }
    
    /**
     * 根据字典类型ID更新缓存
     * @param dictTypeId 字典类型ID
     */
    private void refreshCacheByDictTypeId(Long dictTypeId) {
        if (dictTypeId == null) {
            return;
        }
        
        // 获取字典类型信息
        DictType dictType = baseMapper.selectById(dictTypeId);
        if (dictType == null || dictType.getStatus() != 1) {
            // 如果字典类型不存在或被禁用，从缓存中移除
            if (dictType != null) {
                dictCache.remove(dictType.getDictType());
            }
            return;
        }
        
        // 查询该字典类型下的所有启用数据
        List<DictData> dataList = dictDataMapper.selectList(
            new LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictTypeId, dictTypeId)
                .eq(DictData::getStatus, 1)
                .orderByAsc(DictData::getDictSort)
        );
        
        // 更新缓存
        dictCache.put(dictType.getDictType(), dataList);
    }
    
    @Override
    public boolean hasDictData(Long dictTypeId) {
        if (dictTypeId == null) {
            return false;
        }
        Long dataCount = dictDataMapper.selectCount(
            new LambdaQueryWrapper<DictData>().eq(DictData::getDictTypeId, dictTypeId)
        );
        return dataCount != null && dataCount > 0;
    }
}
