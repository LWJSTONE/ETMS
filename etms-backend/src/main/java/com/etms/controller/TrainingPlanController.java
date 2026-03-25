package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.TrainingPlan;
import com.etms.service.TrainingPlanService;
import com.etms.vo.TrainingPlanVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import com.etms.exception.BusinessException;

/**
 * 培训计划控制器
 */
@Api(tags = "培训计划管理")
@RestController
@RequestMapping("/training/plans")
@RequiredArgsConstructor
@Validated
public class TrainingPlanController {
    
    private final TrainingPlanService trainingPlanService;
    
    @ApiOperation(value = "分页查询培训计划列表")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<TrainingPlanVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 100, message = "每页数量不能超过100") Long size,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer planType,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式必须为yyyy-MM-dd") String startDate,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式必须为yyyy-MM-dd") String endDate,
            @RequestParam(required = false) Long deptId) {
        Page<TrainingPlan> page = new Page<>(current, size);
        Page<TrainingPlanVO> voPage = trainingPlanService.pagePlans(page, planName, status, planType, startDate, endDate, deptId);
        PageResult<TrainingPlanVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取培训计划详情")
    @GetMapping("/{id}")
    public Result<TrainingPlanVO> get(@PathVariable Long id) {
        TrainingPlanVO vo = trainingPlanService.getPlanDetail(id);
        if (vo == null) {
            throw new BusinessException("培训计划不存在");
        }
        return Result.success(vo);
    }
    
    @ApiOperation(value = "新增培训计划")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> add(@Valid @RequestBody TrainingPlan plan) {
        trainingPlanService.addPlan(plan);
        return Result.success();
    }
    
    @ApiOperation(value = "更新培训计划")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TrainingPlan plan) {
        plan.setId(id);
        trainingPlanService.updatePlan(plan);
        return Result.success();
    }
    
    @ApiOperation(value = "删除培训计划")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        // 修复：删除培训计划前检查是否有学员参与
        if (trainingPlanService.hasParticipants(id)) {
            return Result.error("该培训计划已有学员参与，无法删除");
        }
        trainingPlanService.deletePlan(id);
        return Result.success();
    }
    
    @ApiOperation(value = "发布培训计划")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> publish(@PathVariable Long id) {
        trainingPlanService.publishPlan(id);
        return Result.success();
    }
    
    @ApiOperation(value = "归档培训计划")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> archive(@PathVariable Long id) {
        trainingPlanService.archivePlan(id);
        return Result.success();
    }
    
    @ApiOperation(value = "结束培训计划")
    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINING_MANAGER')")
    public Result<Void> end(@PathVariable Long id) {
        trainingPlanService.endPlan(id);
        return Result.success();
    }
}
