import request from '@/utils/request'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 获取部门树形结构
export function getDeptTree(): Promise<ApiResponse<any[]>> {
  return request.get('/system/depts/tree')
}

// 获取部门列表
export function getDeptList(parentId?: number): Promise<ApiResponse<any[]>> {
  return request.get('/system/depts', { params: { parentId } })
}

// 获取部门详情
export function getDeptDetail(id: number): Promise<ApiResponse<any>> {
  return request.get(`/system/depts/${id}`)
}

// 新增部门
export function createDept(data: any): Promise<ApiResponse<void>> {
  return request.post('/system/depts', data)
}

// 更新部门
export function updateDept(id: number, data: any): Promise<ApiResponse<void>> {
  return request.put(`/system/depts/${id}`, data)
}

// 删除部门
export function deleteDept(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/system/depts/${id}`)
}
