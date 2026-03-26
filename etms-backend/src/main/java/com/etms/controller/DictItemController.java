package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
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
 * 字典数据项控制器
 * 用于管理字典数据（DictData）
 */
@Api(tags = "字典数据项管理")
@RestController
@RequestMapping("/system/dict/item")
@RequiredArgsConstructor
@Validated
public class DictItemController {
    
    private final DictService dictService;
    
    @ApiOperation(value = "分页查询字典数据列表")
    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/page")
    public Result<PageResult<DictData>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long dictTypeId,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) Integer status) {
        
        Page<DictData> page = new Page<>(current, size);
        Page<DictData> resultPage = dictService.pageDictData(page, dictTypeId, dictLabel, status);
        
        PageResult<DictData> pageResult = new PageResult<>(
                resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取字典数据详情")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/{id}")
    public Result<DictData> getById(@PathVariable Long id) {
        DictData dictData = dictService.getDictDataById(id);
        if (dictData == null) {
            return Result.error(404, "字典数据不存在");
        }
        return Result.success(dictData);
    }
    
    @ApiOperation(value = "根据字典类型ID获取字典数据列表")
    @GetMapping("/type/{dictTypeId}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DictData>> getByTypeId(@PathVariable Long dictTypeId) {
        List<DictData> list = dictService.getDictDataList(dictTypeId);
        return Result.success(list);
    }
    
    @ApiOperation(value = "根据字典类型编码获取字典数据列表")
    @GetMapping("/code/{dictType}")
    @PreAuthorize("isAuthenticated()")
    public Result<List<DictData>> getByTypeCode(@PathVariable String dictType) {
        List<DictData> list = dictService.getDictDataByType(dictType);
        return Result.success(list);
    }
    
    @ApiOperation(value = "新增字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody DictData dictData) {
        dictService.addDictData(dictData);
        return Result.success();
    }
    
    @ApiOperation(value = "更新字典数据")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DictData dictData) {
        dictData.setId(id);
        dictService.updateDictData(dictData);
        return Result.success();
    }
    
    @ApiOperation(value = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return Result.success();
    }
}
