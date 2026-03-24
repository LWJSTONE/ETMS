package com.etms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("sys_operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户名 */
    private String username;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String operationType;

    /** 操作描述 */
    private String operationDesc;

    /** 请求方法 */
    private String requestMethod;

    /** 请求URL */
    private String requestUrl;

    /** 请求参数 */
    private String requestParams;

    /** 操作IP */
    private String ipAddress;

    /** IP归属地 */
    private String ipLocation;

    /** 浏览器信息 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 操作状态(0失败 1成功) */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;

    /** 执行耗时(毫秒) */
    private Integer costTime;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
