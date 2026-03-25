package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.DictType;
import com.etms.entity.DictData;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService extends IService<DictType> {
    
    /**
     * 分页查询字典类型列表
     */
    Page<DictType> pageDictTypes(Page<DictType> page, String dictName, String dictType, Integer status);
    
    /**
     * 获取字典类型详情
     */
    DictType getDictTypeDetail(Long id);
    
    /**
     * 新增字典类型
     */
    void addDictType(DictType dictType);
    
    /**
     * 更新字典类型
     */
    void updateDictType(DictType dictType);
    
    /**
     * 删除字典类型
     */
    void deleteDictType(Long id);
    
    /**
     * 获取字典数据列表
     */
    List<DictData> getDictDataList(Long dictTypeId);
    
    /**
     * 新增字典数据
     */
    void addDictData(DictData dictData);
    
    /**
     * 更新字典数据
     */
    void updateDictData(DictData dictData);
    
    /**
     * 删除字典数据
     */
    void deleteDictData(Long id);
    
    /**
     * 根据字典类型获取字典数据
     */
    List<DictData> getDictDataByType(String dictType);
    
    /**
     * 刷新字典缓存
     */
    void refreshCache();
    
    /**
     * 检查字典类型是否有字典数据
     * @param dictTypeId 字典类型ID
     * @return 是否有字典数据
     */
    boolean hasDictData(Long dictTypeId);
    
    /**
     * 根据ID获取字典数据详情
     * @param id 字典数据ID
     * @return 字典数据
     */
    DictData getDictDataById(Long id);
    
    /**
     * 分页查询字典数据
     * @param page 分页参数
     * @param dictTypeId 字典类型ID
     * @param dictLabel 字典标签（模糊查询）
     * @param status 状态
     * @return 分页结果
     */
    Page<DictData> pageDictData(Page<DictData> page, Long dictTypeId, String dictLabel, Integer status);
}
