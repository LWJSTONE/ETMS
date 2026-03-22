import request from '@/utils/request'
import type { ApiResponse, PageResult, PageParams } from './types'

// 系统配置类型定义
export interface Config {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: string
  status: number
  remark: string
  createTime: string
}

// 获取配置列表
export function getConfigList(params: PageParams & { configName?: string; configKey?: string; status?: number }): Promise<ApiResponse<PageResult<Config>>> {
  return request.get('/system/configs', params)
}

// 获取配置详情
export function getConfigDetail(id: number): Promise<ApiResponse<Config>> {
  return request.get(`/system/configs/${id}`)
}

// 根据配置键名获取配置值
export function getConfigValue(configKey: string): Promise<ApiResponse<string>> {
  return request.get(`/system/configs/key/${configKey}`)
}

// 新增配置
export function createConfig(data: Config): Promise<ApiResponse<void>> {
  return request.post('/system/configs', data)
}

// 更新配置
export function updateConfig(id: number, data: Config): Promise<ApiResponse<void>> {
  return request.put(`/system/configs/${id}`, data)
}

// 删除配置
export function deleteConfig(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/system/configs/${id}`)
}

// 刷新缓存
export function refreshConfigCache(): Promise<ApiResponse<void>> {
  return request.post('/system/configs/refresh-cache')
}
