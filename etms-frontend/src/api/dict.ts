import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  DictTypeCreateParams,
  DictTypeUpdateParams,
  DictDataCreateParams,
  DictDataUpdateParams
} from './types'

// 字典类型定义
export interface DictType {
  id: number
  dictName: string
  dictType: string
  status: number
  remark: string
  createTime: string
}

// 字典数据定义
export interface DictData {
  id: number
  dictTypeId: number
  dictLabel: string
  dictValue: string
  dictSort: number
  status: number
  remark: string
  createTime: string
}

// 修复：重导出类型，方便其他模块使用
export type { DictType, DictData }

// 获取字典类型列表
export function getDictTypeList(params: PageParams & { dictName?: string; dictType?: string; status?: number }): Promise<PageResult<DictType>> {
  return request.get('/system/dict/types', params)
}

// 获取字典类型详情
export function getDictTypeDetail(id: number): Promise<DictType> {
  return request.get(`/system/dict/types/${id}`)
}

// 新增字典类型
export function createDictType(data: DictTypeCreateParams): Promise<void> {
  return request.post('/system/dict/types', data)
}

// 更新字典类型
export function updateDictType(id: number, data: DictTypeUpdateParams): Promise<void> {
  return request.put(`/system/dict/types/${id}`, data)
}

// 删除字典类型
export function deleteDictType(id: number): Promise<void> {
  return request.delete(`/system/dict/types/${id}`)
}

// 获取字典数据列表
export function getDictDataList(dictTypeId: number): Promise<DictData[]> {
  return request.get(`/system/dict/data/${dictTypeId}`)
}

// 根据字典类型获取字典数据
export function getDictDataByType(dictType: string): Promise<DictData[]> {
  return request.get(`/system/dict/data/type/${dictType}`)
}

// 新增字典数据
export function createDictData(data: DictDataCreateParams): Promise<void> {
  return request.post('/system/dict/data', data)
}

// 更新字典数据
export function updateDictData(id: number, data: DictDataUpdateParams): Promise<void> {
  return request.put(`/system/dict/data/${id}`, data)
}

// 删除字典数据
export function deleteDictData(id: number): Promise<void> {
  return request.delete(`/system/dict/data/${id}`)
}

// 刷新字典缓存
export function refreshDictCache(): Promise<void> {
  return request.post('/system/dict/cache/refresh')
}
