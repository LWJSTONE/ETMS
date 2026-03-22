package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Position;
import com.etms.service.PositionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 岗位管理控制器
 */
@Api(tags = "岗位管理")
@RestController
@RequestMapping("/system/positions")
@RequiredArgsConstructor
@Validated
public class PositionController {
    
    private final PositionService positionService;
    
    @ApiOperation(value = "分页查询岗位列表")
    @GetMapping
    public Result<PageResult<Position>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer status) {
        Page<Position> page = new Page<>(current, size);
        Page<Position> resultPage = positionService.pagePositions(page, positionName, positionCode, status);
        PageResult<Position> pageResult = new PageResult<>(
                resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取岗位详情")
    @GetMapping("/{id}")
    public Result<Position> get(@PathVariable Long id) {
        Position position = positionService.getPositionDetail(id);
        return Result.success(position);
    }
    
    @ApiOperation(value = "新增岗位")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Position position) {
        positionService.addPosition(position);
        return Result.success();
    }
    
    @ApiOperation(value = "更新岗位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Position position) {
        position.setId(id);
        positionService.updatePosition(position);
        return Result.success();
    }
    
    @ApiOperation(value = "删除岗位")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Position position) {
        Position updatePosition = new Position();
        updatePosition.setId(id);
        updatePosition.setStatus(position.getStatus());
        positionService.updateById(updatePosition);
        return Result.success();
    }
}
