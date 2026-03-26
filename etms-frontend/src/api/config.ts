import request from '@/utils/request'
import type { PageResult, PageParams } from './types'

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

// 修复：重导出类型，方便其他模块使用
export type { Config }

// 新增/更新配置参数类型
export interface ConfigParams {
  configName: string
  configKey: string
  configValue: string
  configType: string
  status: number
  sortOrder?: number
  remark?: string
}

// 获取配置列表
export function getConfigList(params: PageParams & { configName?: string; configKey?: string; status?: number }): Promise<PageResult<Config>> {
  return request.get('/system/configs', params)
}

// 获取配置详情
export function getConfigDetail(id: number): Promise<Config> {
  return request.get(`/system/configs/${id}`)
}

// 根据配置键名获取配置值
export function getConfigValue(configKey: string): Promise<string> {
  return request.get(`/system/configs/key/${configKey}`)
}

// 新增配置
export function createConfig(data: ConfigParams): Promise<void> {
  return request.post('/system/configs', data)
}

// 更新配置
export function updateConfig(id: number, data: ConfigParams): Promise<void> {
  return request.put(`/system/configs/${id}`, data)
}

// 删除配置
export function deleteConfig(id: number): Promise<void> {
  return request.delete(`/system/configs/${id}`)
}

// 刷新缓存
export function refreshConfigCache(): Promise<void> {
  return request.post('/system/configs/refresh-cache')
}
