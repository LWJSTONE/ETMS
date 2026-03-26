import request from '@/utils/request'
import type { PageResult, PageParams } from './types'

/**
 * 日志相关类型定义
 */

// 日志项类型
export interface LogItem {
  id: number  // 修复：后端返回的字段名是id，不是logId
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

// 修复：重导出类型，方便其他模块使用
export type { LogItem }

// 日志搜索参数
export interface LogSearchParams extends PageParams {
  module?: string
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
export function getLogList(params: LogSearchParams): Promise<PageResult<LogItem>> {
  return request.get('/system/logs', params)
}

// 获取日志详情
export function getLogDetail(id: number): Promise<LogItem> {
  return request.get(`/system/logs/${id}`)
}

// 清空日志参数
export interface ClearLogsParams {
  startTime?: string
  endTime?: string
  confirm?: boolean  // 修复：添加confirm参数，用于确认清空全部日志的危险操作
}

// 清空日志
export function clearLogs(params: ClearLogsParams): Promise<void> {
  return request.delete('/system/logs', params)
}

// 导出日志
export function exportLogs(params: LogSearchParams): Promise<Blob> {
  return request.getBlob('/system/logs/export', params)
}
