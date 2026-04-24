package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.dto.SubmitExamDTO;
import com.etms.entity.ExamRecord;
import com.etms.exception.BusinessException;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 考试记录控制器
 */
@Api(tags = "考试记录管理")
@RestController
@RequestMapping("/exam/records")
@RequiredArgsConstructor
@Validated
public class ExamRecordController {
    
    private final ExamRecordService examRecordService;
    
    @ApiOperation(value = "分页查询考试记录")
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<PageResult<ExamRecordVO>> page(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String paperName) {
        Page<ExamRecord> page = new Page<>(current, size);
        Page<ExamRecordVO> voPage = examRecordService.pageExamRecords(page, paperId, userId, status, userName, paperName);
        PageResult<ExamRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "获取考试记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<ExamRecordVO> get(@PathVariable Long id) {
        // 权限校验：用户可以查看自己的考试记录详情，管理员可以查看所有
        ExamRecordVO vo = examRecordService.getExamRecordDetailForUser(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "开始考试")
    @PostMapping("/start/{paperId}")
    @PreAuthorize("isAuthenticated()")  // 修复：添加权限控制
    public Result<ExamRecordVO> startExam(
            @PathVariable Long paperId,
            @RequestParam(required = false) Long planId) {
        // 修复：返回ExamRecordVO而非ExamRecord，确保前端能获取到paperName、passScore、duration等关键字段
        // 资格校验已在 startExam 方法内部完成，包括：
        // - 试卷存在性和状态校验
        // - 考试时间窗口校验
        // - 是否有进行中的考试校验
        // - 考试次数限制校验
        ExamRecordVO vo = examRecordService.startExamVO(paperId, planId);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "提交试卷")
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> submitExam(@Valid @RequestBody SubmitExamDTO submitExamDTO) {
        examRecordService.submitExam(submitExamDTO.getRecordId(), submitExamDTO.getAnswers());
        return Result.success();
    }
    
    @ApiOperation(value = "放弃考试")
    @PostMapping("/giveup/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> giveUpExam(@PathVariable Long recordId) {
        examRecordService.giveUpExam(recordId);
        return Result.success();
    }
    
    @ApiOperation(value = "获取当前用户的考试记录")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<ExamRecordVO>> pageMy(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long current,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") @Max(value = 10000, message = "每页数量不能超过10000") Long size,
            @RequestParam(required = false) Integer status) {
        Page<ExamRecord> page = new Page<>(current, size);
        Page<ExamRecordVO> voPage = examRecordService.pageMyExamRecords(page, status);
        PageResult<ExamRecordVO> pageResult = new PageResult<>(
                voPage.getRecords(), voPage.getTotal(), voPage.getCurrent(), voPage.getSize()
        );
        return Result.success(pageResult);
    }
    
    @ApiOperation(value = "导出考试记录")
    @GetMapping("/export")
    @PreAuthorize("hasRole('admin')")
    public void export(
            @RequestParam(required = false) Long paperId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String paperName,
            HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("考试记录_" + java.time.LocalDate.now(), StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            
            // 导出数据
            examRecordService.exportRecords(paperId, userId, status, userName, paperName, response.getOutputStream());
        } catch (Exception e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }
}
