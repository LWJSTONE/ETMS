package com.etms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.etms.entity.OperationLog;

import javax.servlet.http.HttpServletResponse;

/**
 * 操作日志服务接口
 */
public interface LogService extends IService<OperationLog> {

    /**
     * 分页查询日志列表
     * @param page 分页参数
     * @param module 操作模块
     * @param operationType 操作类型
     * @param operator 操作人员
     * @param status 操作状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<OperationLog> pageLogs(Page<OperationLog> page, String module, String operationType,
                                 String operator, Integer status, String startTime, String endTime);

    /**
     * 获取日志详情
     * @param id 日志ID
     * @return 日志详情
     */
    OperationLog getLogDetail(Long id);

    /**
     * 清空日志
     * @param startTime 开始时间（可选，格式：yyyy-MM-dd）
     * @param endTime 结束时间（可选，格式：yyyy-MM-dd）
     * @return 是否成功
     */
    boolean clearLogs(String startTime, String endTime);

    /**
     * 导出日志
     * @param module 操作模块
     * @param operationType 操作类型
     * @param operator 操作人员
     * @param status 操作状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param response HTTP响应
     */
    void exportLogs(String module, String operationType, String operator, Integer status,
                    String startTime, String endTime, HttpServletResponse response);
}
