import request from '@/utils/request'
import type {
  ApiResponse,
  Dept,
  DeptCreateParams,
  DeptUpdateParams
} from './types'

// 获取部门树形结构
export function getDeptTree(): Promise<ApiResponse<Dept[]>> {
  return request.get('/system/depts/tree-structure')
}

// 获取部门列表
export function getDeptList(parentId?: number): Promise<ApiResponse<Dept[]>> {
  return request.get('/system/depts', parentId ? { parentId } : undefined)
}

// 获取部门列表（公开接口，无需权限）
export function getPublicDeptList(): Promise<ApiResponse<Dept[]>> {
  return request.get('/system/depts/public/list')
}

// 获取部门树形结构（公开接口，无需权限）
export function getPublicDeptTree(): Promise<ApiResponse<Dept[]>> {
  return request.get('/system/depts/public/tree')
}

// 获取部门详情
export function getDeptDetail(id: number): Promise<ApiResponse<Dept>> {
  return request.get(`/system/depts/${id}`)
}

// 新增部门
export function createDept(data: DeptCreateParams): Promise<ApiResponse<void>> {
  return request.post('/system/depts', data)
}

// 更新部门
export function updateDept(id: number, data: DeptUpdateParams): Promise<ApiResponse<void>> {
  return request.put(`/system/depts/${id}`, data)
}

// 删除部门
export function deleteDept(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/system/depts/${id}`)
}
