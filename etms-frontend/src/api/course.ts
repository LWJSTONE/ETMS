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

// 获取课程列表
export function getCourseList(params: PageParams): Promise<ApiResponse<PageResult<any>>> {
  return request.get('/courses', params)
}

// 获取课程详情
export function getCourseDetail(id: number): Promise<ApiResponse<any>> {
  return request.get(`/courses/${id}`)
}

// 新增课程
export function createCourse(data: any): Promise<ApiResponse<void>> {
  return request.post('/courses', data)
}

// 更新课程
export function updateCourse(id: number, data: any): Promise<ApiResponse<void>> {
  return request.put(`/courses/${id}`, data)
}

// 删除课程
export function deleteCourse(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/courses/${id}`)
}

// 提交审核
export function submitCourseAudit(id: number): Promise<ApiResponse<void>> {
  return request.post(`/courses/${id}/submit`)
}

// 审核课程
export function auditCourse(id: number, status: number, auditRemark?: string): Promise<ApiResponse<void>> {
  return request.post(`/courses/${id}/audit`, null, { params: { status, auditRemark } })
}

// 上架课程
export function publishCourse(id: number): Promise<ApiResponse<void>> {
  return request.post(`/courses/${id}/publish`)
}

// 下架课程
export function unpublishCourse(id: number): Promise<ApiResponse<void>> {
  return request.post(`/courses/${id}/unpublish`)
}

// 获取课程列表（不分页）
export function getCourseListAll(categoryId?: number): Promise<ApiResponse<any[]>> {
  return request.get('/courses/list', { categoryId })
}
