package com.etms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etms.entity.OperationLog;
import com.etms.exception.BusinessException;
import com.etms.mapper.OperationLogMapper;
import com.etms.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements LogService {

    @Override
    public Page<OperationLog> pageLogs(Page<OperationLog> page, String module, String operationType,
                                        String operator, Integer status, String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(module), OperationLog::getModule, module)
               .eq(StringUtils.hasText(operationType), OperationLog::getOperationType, operationType)
               .like(StringUtils.hasText(operator), OperationLog::getUsername, operator)
               .eq(status != null, OperationLog::getStatus, status)
               .orderByDesc(OperationLog::getCreateTime);

        // 时间范围查询
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endTime + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.between(OperationLog::getCreateTime, start, end);
        }

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public OperationLog getLogDetail(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearLogs(String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 修复：改为按时间范围删除，必须提供时间参数
        if (startTime == null || startTime.isEmpty() || endTime == null || endTime.isEmpty()) {
            throw new BusinessException("请指定要删除的日志时间范围");
        }
        
        try {
            LocalDateTime start = LocalDateTime.parse(startTime + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endTime + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.between(OperationLog::getCreateTime, start, end);
        } catch (Exception e) {
            throw new BusinessException("时间格式错误，请使用 yyyy-MM-dd 格式");
        }
        
        return baseMapper.delete(wrapper) >= 0;
    }

    @Override
    public void exportLogs(String module, String operationType, String operator, Integer status,
                           String startTime, String endTime, HttpServletResponse response) {
        // 查询数据
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(module), OperationLog::getModule, module)
               .eq(StringUtils.hasText(operationType), OperationLog::getOperationType, operationType)
               .like(StringUtils.hasText(operator), OperationLog::getUsername, operator)
               .eq(status != null, OperationLog::getStatus, status)
               .orderByDesc(OperationLog::getCreateTime);

        // 时间范围查询
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(endTime + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.between(OperationLog::getCreateTime, start, end);
        }

        List<OperationLog> logs = baseMapper.selectList(wrapper);

        // 创建Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("操作日志");

            // 创建标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 创建表头
            String[] headers = {"日志编号", "操作模块", "操作类型", "操作人员", "操作IP", "操作状态", "操作时间", "操作描述"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // 创建数据样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 填充数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (OperationLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getLogId() != null ? log.getLogId() : 0);
                row.createCell(1).setCellValue(log.getModule() != null ? log.getModule() : "");
                row.createCell(2).setCellValue(log.getOperationType() != null ? log.getOperationType() : "");
                row.createCell(3).setCellValue(log.getUsername() != null ? log.getUsername() : "");
                row.createCell(4).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                row.createCell(5).setCellValue(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
                row.createCell(6).setCellValue(log.getCreateTime() != null ? log.getCreateTime().format(formatter) : "");
                row.createCell(7).setCellValue(log.getOperationDesc() != null ? log.getOperationDesc() : "");
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 写入响应
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("导出日志失败", e);
            throw new BusinessException("导出日志失败: " + e.getMessage());
        }
    }
}
