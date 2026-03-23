package com.etms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.etms.common.PageResult;
import com.etms.common.Result;
import com.etms.entity.ExamRecord;
import com.etms.service.ExamRecordService;
import com.etms.vo.ExamRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
    public Result<PageResult<ExamRecordVO>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
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
    public Result<ExamRecordVO> get(@PathVariable Long id) {
        ExamRecordVO vo = examRecordService.getExamRecordDetail(id);
        return Result.success(vo);
    }
    
    @ApiOperation(value = "开始考试")
    @PostMapping("/start/{paperId}")
    public Result<ExamRecord> startExam(
            @PathVariable Long paperId,
            @RequestParam(required = false) Long planId) {
        ExamRecord record = examRecordService.startExam(paperId, planId);
        return Result.success(record);
    }
    
    @ApiOperation(value = "提交试卷")
    @PostMapping("/submit")
    public Result<Void> submitExam(@RequestBody Map<String, Object> params) {
        // 参数验证
        if (params.get("recordId") == null) {
            return Result.error("考试记录ID不能为空");
        }
        if (params.get("answers") == null) {
            return Result.error("答案不能为空");
        }

        Long recordId = Long.valueOf(params.get("recordId").toString());
        String answers = params.get("answers").toString();
        examRecordService.submitExam(recordId, answers);
        return Result.success();
    }
    
    @ApiOperation(value = "放弃考试")
    @PostMapping("/giveup/{recordId}")
    public Result<Void> giveUpExam(@PathVariable Long recordId) {
        examRecordService.giveUpExam(recordId);
        return Result.success();
    }
    
    @ApiOperation(value = "获取当前用户的考试记录")
    @GetMapping("/my")
    public Result<PageResult<ExamRecordVO>> pageMy(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
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
            throw new RuntimeException("导出失败", e);
        }
    }
}
