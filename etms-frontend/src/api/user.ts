import request from '@/utils/request'
import type {
  ApiResponse,
  PageResult,
  PageParams,
  User,
  UserCreateParams,
  UserUpdateParams
} from './types'

// 获取用户列表
export function getUserList(params: PageParams): Promise<ApiResponse<PageResult<User>>> {
  return request.get('/system/users', params)
}

// 获取用户详情
export function getUserDetail(id: number): Promise<ApiResponse<User>> {
  return request.get(`/system/users/${id}`)
}

// 新增用户
export function createUser(data: UserCreateParams): Promise<ApiResponse<void>> {
  return request.post('/system/users', data)
}

// 更新用户
export function updateUser(id: number, data: UserUpdateParams): Promise<ApiResponse<void>> {
  return request.put(`/system/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/system/users/${id}`)
}

// 重置密码
export function resetPassword(id: number): Promise<ApiResponse<void>> {
  return request.put(`/system/users/${id}/reset-password`)
}

// 修改状态
export function updateUserStatus(id: number, status: number): Promise<ApiResponse<void>> {
  return request.put(`/system/users/${id}/status`, { status })
}

// 分配角色
export function assignRoles(id: number, roleIds: number[]): Promise<ApiResponse<void>> {
  return request.put(`/system/users/${id}/roles`, roleIds)
}

// 修改密码
export function updatePassword(id: number, oldPassword: string, newPassword: string): Promise<ApiResponse<void>> {
  return request.put(`/system/users/${id}/password`, null, {
    params: { oldPassword, newPassword }
  })
}
