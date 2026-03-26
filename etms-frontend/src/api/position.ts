import request from '@/utils/request'
import type { PageResult, PageParams } from './types'

// 修复：重导出类型，方便其他模块使用
export type { Position as PositionType }

// 岗位类型定义
export interface Position {
  id: number
  positionName: string
  positionCode: string
  sortOrder: number
  status: number
  remark: string
  createTime: string
  updateTime: string
}

export interface PositionCreateParams {
  positionName: string
  positionCode: string
  sortOrder?: number
  status?: number
  remark?: string
}

export interface PositionUpdateParams {
  positionName?: string
  positionCode?: string
  sortOrder?: number
  status?: number
  remark?: string
}

// 获取岗位列表
export function getPositionList(params: PageParams & { positionName?: string; positionCode?: string; status?: number }): Promise<PageResult<Position>> {
  return request.get('/system/positions', params)
}

// 获取岗位详情
export function getPositionDetail(id: number): Promise<Position> {
  return request.get(`/system/positions/${id}`)
}

// 新增岗位
export function createPosition(data: PositionCreateParams): Promise<void> {
  return request.post('/system/positions', data)
}

// 更新岗位
export function updatePosition(id: number, data: PositionUpdateParams): Promise<void> {
  return request.put(`/system/positions/${id}`, data)
}

// 删除岗位
export function deletePosition(id: number): Promise<void> {
  return request.delete(`/system/positions/${id}`)
}

// 修改状态
export function updatePositionStatus(id: number, status: number): Promise<void> {
  return request.put(`/system/positions/${id}/status`, { status })
}

// 导出岗位
export function exportPositions(params: PageParams & { positionName?: string; positionCode?: string; status?: number }): Promise<Blob> {
  return request.getBlob('/system/positions/export', params)
}

// 获取所有岗位列表(不分页)
export function getPositionListAll(): Promise<Position[]> {
  return request.get('/system/positions/all')
}
