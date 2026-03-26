import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  User,
  UserCreateParams,
  UserUpdateParams
} from './types'

// 修复：重导出类型，方便其他模块使用
export type { User }

// 用户查询参数类型
export interface UserQueryParams extends PageParams {
  username?: string
  realName?: string
  deptId?: number
  status?: number
}

// 获取用户列表
export function getUserList(params: UserQueryParams): Promise<PageResult<User>> {
  return request.get('/system/users', params)
}

// 获取用户详情
export function getUserDetail(id: number): Promise<User> {
  return request.get(`/system/users/${id}`)
}

// 新增用户
export function createUser(data: UserCreateParams): Promise<void> {
  return request.post('/system/users', data)
}

// 更新用户
export function updateUser(id: number, data: UserUpdateParams): Promise<void> {
  return request.put(`/system/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number): Promise<void> {
  return request.delete(`/system/users/${id}`)
}

// 重置密码 - 返回新密码
export function resetPassword(id: number): Promise<string> {
  return request.put(`/system/users/${id}/reset-password`)
}

// 修改状态
export function updateUserStatus(id: number, status: number): Promise<void> {
  return request.put(`/system/users/${id}/status`, { status })
}

// 分配角色
export function assignRoles(id: number, roleIds: number[]): Promise<void> {
  return request.put(`/system/users/${id}/roles`, roleIds)
}

// 修改密码
export function updatePassword(id: number, oldPassword: string, newPassword: string): Promise<void> {
  return request.put(`/system/users/${id}/password`, { oldPassword, newPassword })
}

// 导出用户
export function exportUsers(params: PageParams): Promise<Blob> {
  return request.getBlob('/system/users/export', params)
}
