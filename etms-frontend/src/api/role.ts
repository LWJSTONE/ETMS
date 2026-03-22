import request from '@/utils/request'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

interface PageParams {
  current?: number
  size?: number
  [key: string]: any
}

// 获取角色列表
export function getRoleList(params: PageParams): Promise<ApiResponse<PageResult<any>>> {
  return request.get('/system/roles', { params })
}

// 获取所有角色列表(不分页)
export function getRoleListAll(): Promise<ApiResponse<any[]>> {
  return request.get('/system/roles/list')
}

// 获取角色详情
export function getRoleDetail(id: number): Promise<ApiResponse<any>> {
  return request.get(`/system/roles/${id}`)
}

// 新增角色
export function createRole(data: any): Promise<ApiResponse<void>> {
  return request.post('/system/roles', data)
}

// 更新角色
export function updateRole(id: number, data: any): Promise<ApiResponse<void>> {
  return request.put(`/system/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/system/roles/${id}`)
}

// 分配权限
export function assignPermissions(id: number, permissionIds: number[]): Promise<ApiResponse<void>> {
  return request.put(`/system/roles/${id}/permissions`, permissionIds)
}

// 获取角色权限ID列表
export function getRolePermissions(id: number): Promise<ApiResponse<number[]>> {
  return request.get(`/system/roles/${id}/permissions`)
}
