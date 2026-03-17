package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.TrainingPlan;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 培训计划控制器
 */
@Tag(name = "培训计划管理")
@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class TrainingPlanController {
    
    private final TrainingPlanService trainingPlanService;
    
    @Operation(summary = "分页查询培训计划列表")
    @GetMapping
    public Result<PageResult<TrainingPlanVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer planType) {
        Page<TrainingPlan> page = new Page<>(current, size);
        Page<TrainingPlanVO> voPage = trainingPlanService.pagePlans(page, planName, status, planType);
        PageResult<TrainingPlanVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @Operation(summary = "获取培训计划详情")
    @GetMapping("/{id}")
    public Result<TrainingPlanVO> get(@PathVariable Long id) {
        TrainingPlanVO vo = trainingPlanService.getPlanDetail(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增培训计划")
    @PostMapping
    public Result<Void> add(@RequestBody TrainingPlan plan) {
        trainingPlanService.addPlan(plan);
        return Result.success();
    }
    
    @Operation(summary = "更新培训计划")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody TrainingPlan plan) {
        plan.setId(id);
        trainingPlanService.updatePlan(plan);
        return Result.success();
    }
    
    @Operation(summary = "删除培训计划")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        trainingPlanService.deletePlan(id);
        return Result.success();
    }
    
    @Operation(summary = "发布培训计划")
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        trainingPlanService.publishPlan(id);
        return Result.success();
    }
    
    @Operation(summary = "归档培训计划")
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        trainingPlanService.archivePlan(id);
        return Result.success();
    }
}
