package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.DictType;
import com.etms.entity.DictData;
import com.etms.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 字典管理控制器
 */
@Api(tags = "字典管理")
@RestController
@RequiredArgsConstructor
@Validated
public class DictController {
    
    private final DictService dictService;
    
    // ==================== 字典类型相关 ====================
    
    @ApiOperation(value = "分页查询字典类型列表")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/system/dict/types")
    public Result<PageResult<DictType>> pageDictTypes(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status) {
        Page<DictType> page = new Page<>(current, size);
        Page<DictType> resultPage = dictService.pageDictTypes(page, dictName, dictType, status);
        PageResult<DictType> pageResult = new PageResult<>(
                resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取字典类型详情")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/system/dict/types/{id}")
    public Result<DictType> getDictType(@PathVariable Long id) {
        DictType type = dictService.getDictTypeDetail(id);
        if (type == null) {
            return Result.error("字典类型不存在");
        }
        return Result.success(type);
    }
    
    @ApiOperation(value = "新增字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/system/dict/types")
    public Result<Void> addDictType(@Valid @RequestBody DictType dictType) {
        dictService.addDictType(dictType);
        return Result.success();
    }
    
    @ApiOperation(value = "更新字典类型")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/system/dict/types/{id}")
    public Result<Void> updateDictType(@PathVariable Long id, @Valid @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateDictType(dictType);
        return Result.success();
    }
    
    @ApiOperation(value = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/system/dict/types/{id}")
    public Result<Void> deleteDictType(@PathVariable Long id) {
        // 修复：删除字典类型前检查是否有关联的字典数据
        if (dictService.hasDictData(id)) {
            return Result.error("该字典类型下存在字典数据，无法删除");
        }
        dictService.deleteDictType(id);
        return Result.success();
    }
    
    // ==================== 字典数据相关 ====================
    
    @ApiOperation(value = "获取字典数据列表")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/system/dict/data/{dictTypeId}")
    public Result<List<DictData>> getDictDataList(@PathVariable Long dictTypeId) {
        List<DictData> list = dictService.getDictDataList(dictTypeId);
        return Result.success(list);
    }
    
    @ApiOperation(value = "根据字典类型获取字典数据")
    @GetMapping("/system/dict/data/type/{dictType}")
    public Result<List<DictData>> getDictDataByType(@PathVariable String dictType) {
        List<DictData> list = dictService.getDictDataByType(dictType);
        return Result.success(list);
    }
    
    @ApiOperation(value = "新增字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/system/dict/data")
    public Result<Void> addDictData(@Valid @RequestBody DictData dictData) {
        dictService.addDictData(dictData);
        return Result.success();
    }
    
    @ApiOperation(value = "更新字典数据")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/system/dict/data/{id}")
    public Result<Void> updateDictData(@PathVariable Long id, @Valid @RequestBody DictData dictData) {
        dictData.setId(id);
        dictService.updateDictData(dictData);
        return Result.success();
    }
    
    @ApiOperation(value = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/system/dict/data/{id}")
    public Result<Void> deleteDictData(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return Result.success();
    }
    
    // ==================== 字典缓存相关 ====================
    
    @ApiOperation(value = "刷新字典缓存")
    @PreAuthorize("hasAuthority('system:dict:cache')")
    @PostMapping("/system/dict/cache/refresh")
    public Result<Void> refreshCache() {
        dictService.refreshCache();
        return Result.success();
    }
}
