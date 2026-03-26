import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  Role,
  RoleCreateParams,
  RoleUpdateParams
} from './types'

// 修复：重导出类型，方便其他模块使用
export type { Role }

// 获取角色列表
export function getRoleList(params: PageParams): Promise<PageResult<Role>> {
  return request.get('/system/roles', params)
}

// 获取所有角色列表(不分页)
export function getRoleListAll(): Promise<Role[]> {
  return request.get('/system/roles/all')
}

// 获取角色详情
export function getRoleDetail(id: number): Promise<Role> {
  return request.get(`/system/roles/${id}`)
}

// 新增角色
export function createRole(data: RoleCreateParams): Promise<void> {
  return request.post('/system/roles', data)
}

// 更新角色
export function updateRole(id: number, data: RoleUpdateParams): Promise<void> {
  return request.put(`/system/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: number): Promise<void> {
  return request.delete(`/system/roles/${id}`)
}

// 分配权限
export function assignPermissions(id: number, permissionIds: number[]): Promise<void> {
  return request.put(`/system/roles/${id}/permissions`, permissionIds)
}

// 获取角色权限ID列表
export function getRolePermissions(id: number): Promise<number[]> {
  return request.get(`/system/roles/${id}/permissions`)
}

// 获取权限树
export interface PermissionTreeNode {
  id: number
  permName: string
  permCode?: string
  parentId?: number
  sortOrder?: number
  children?: PermissionTreeNode[]
}

export function getPermissionTree(): Promise<PermissionTreeNode[]> {
  return request.get('/system/permissions/tree')
}

// 修改角色状态
export function updateRoleStatus(id: number, status: number): Promise<void> {
  return request.put(`/system/roles/${id}/status`, { status })
}
