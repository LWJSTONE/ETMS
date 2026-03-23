import request from '@/utils/request'
import type { ApiResponse, PageResult, PageParams } from './types'

/**
 * 日志相关类型定义
 */

// 日志项类型
export interface LogItem {
  logId: number
  userId?: number
  username: string
  module: string
  operationType: string
  operationDesc: string
  requestMethod?: string
  requestUrl?: string
  requestParams?: string
  ipAddress: string
  ipLocation?: string
  browser?: string
  os?: string
  status: number
  errorMsg?: string
  costTime?: number
  createTime: string
}

// 日志搜索参数
export interface LogSearchParams extends PageParams {
  operator?: string
  operationType?: string
  status?: number | null
  startTime?: string
  endTime?: string
}

/**
 * 日志相关API
 */

// 分页查询日志列表
export function getLogList(params: LogSearchParams): Promise<ApiResponse<PageResult<LogItem>>> {
  return request.get('/system/logs', params)
}

// 获取日志详情
export function getLogDetail(id: number): Promise<ApiResponse<LogItem>> {
  return request.get(`/system/logs/${id}`)
}

// 清空日志
export function clearLogs(): Promise<ApiResponse<void>> {
  return request.delete('/system/logs')
}

// 导出日志
export function exportLogs(params: LogSearchParams): Promise<Blob> {
  return request.get('/system/logs/export', params, { responseType: 'blob' })
}
