import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  Course,
  CourseCreateParams,
  CourseUpdateParams,
  CourseAuditParams
} from './types'

// 获取课程列表
export function getCourseList(params: PageParams): Promise<PageResult<Course>> {
  return request.get('/training/courses', params)
}

// 获取课程详情
export function getCourseDetail(id: number): Promise<Course> {
  return request.get(`/training/courses/${id}`)
}

// 新增课程
export function createCourse(data: CourseCreateParams): Promise<void> {
  return request.post('/training/courses', data)
}

// 更新课程
export function updateCourse(id: number, data: CourseUpdateParams): Promise<void> {
  return request.put(`/training/courses/${id}`, data)
}

// 删除课程
export function deleteCourse(id: number): Promise<void> {
  return request.delete(`/training/courses/${id}`)
}

// 提交审核
export function submitCourseAudit(id: number): Promise<void> {
  return request.post(`/training/courses/${id}/submit`)
}

// 审核课程
export function auditCourse(id: number, data: CourseAuditParams): Promise<void> {
  return request.post(`/training/courses/${id}/audit`, data)
}

// 上架课程
export function publishCourse(id: number): Promise<void> {
  return request.post(`/training/courses/${id}/publish`)
}

// 下架课程
export function unpublishCourse(id: number): Promise<void> {
  return request.post(`/training/courses/${id}/unpublish`)
}

// 获取课程列表（不分页）
export function getCourseListAll(categoryId?: number): Promise<Course[]> {
  return request.get('/training/courses/all', { categoryId })
}
