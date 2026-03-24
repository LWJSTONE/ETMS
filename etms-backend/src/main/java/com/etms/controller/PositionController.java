package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.Position;
import com.etms.service.PositionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

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
    @PreAuthorize("hasAuthority('system:position:list')")
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
    @PreAuthorize("hasAuthority('system:position:query')")
    @GetMapping("/{id}")
    public Result<Position> get(@PathVariable Long id) {
        Position position = positionService.getPositionDetail(id);
        if (position == null) {
            return Result.error("岗位不存在");
        }
        return Result.success(position);
    }
    
    @ApiOperation(value = "新增岗位")
    @PreAuthorize("hasAuthority('system:position:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody Position position) {
        positionService.addPosition(position);
        return Result.success();
    }
    
    @ApiOperation(value = "更新岗位")
    @PreAuthorize("hasAuthority('system:position:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Position position) {
        position.setId(id);
        positionService.updatePosition(position);
        return Result.success();
    }
    
    @ApiOperation(value = "删除岗位")
    @PreAuthorize("hasAuthority('system:position:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }
    
    @ApiOperation(value = "修改状态")
    @PreAuthorize("hasAuthority('system:position:edit')")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return Result.error("状态参数不能为空");
        }
        // 验证状态值是否合法
        if (status != 0 && status != 1) {
            return Result.error("状态值不合法，有效值为0(禁用)或1(正常)");
        }
        Position updatePosition = new Position();
        updatePosition.setId(id);
        updatePosition.setStatus(status);
        positionService.updateById(updatePosition);
        return Result.success();
    }

    @ApiOperation(value = "导出岗位")
    @PreAuthorize("hasAuthority('system:position:export')")
    @GetMapping("/export")
    public void export(
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false) String positionCode,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) {
        positionService.exportPositions(positionName, positionCode, status, response);
    }
    
    @ApiOperation(value = "获取所有岗位列表")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('system:position:list')")
    public Result<java.util.List<Position>> listAll() {
        java.util.List<Position> list = positionService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, 1)
                .orderByAsc(Position::getSortOrder)
        );
        return Result.success(list);
    }
}
