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

// 获取用户列表
export function getUserList(params: PageParams): Promise<ApiResponse<PageResult<any>>> {
  return request.get('/users', params)
}

// 获取用户详情
export function getUserDetail(id: number): Promise<ApiResponse<any>> {
  return request.get(`/users/${id}`)
}

// 新增用户
export function createUser(data: any): Promise<ApiResponse<void>> {
  return request.post('/users', data)
}

// 更新用户
export function updateUser(id: number, data: any): Promise<ApiResponse<void>> {
  return request.put(`/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/users/${id}`)
}

// 重置密码
export function resetPassword(id: number): Promise<ApiResponse<void>> {
  return request.put(`/users/${id}/reset-password`)
}

// 修改状态
export function updateUserStatus(id: number, status: number): Promise<ApiResponse<void>> {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}

// 分配角色
export function assignRoles(id: number, roleIds: number[]): Promise<ApiResponse<void>> {
  return request.put(`/users/${id}/roles`, roleIds)
}
