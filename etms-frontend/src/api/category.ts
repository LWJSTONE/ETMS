import request from '@/utils/request'
import type {
  Category,
  CategoryCreateParams,
  CategoryUpdateParams
} from './types'

// 修复：重导出类型，方便其他模块使用
export type { Category }

// 获取分类树形结构
export function getCategoryTree(categoryType?: number): Promise<Category[]> {
  return request.get('/training/categories/tree', categoryType ? { categoryType } : undefined)
}

// 获取分类列表
export function getCategoryList(categoryType?: number, parentId?: number): Promise<Category[]> {
  const params: Record<string, any> = {}
  if (categoryType !== undefined) params.categoryType = categoryType
  if (parentId !== undefined) params.parentId = parentId
  return request.get('/training/categories', params)
}

// 获取分类详情
export function getCategoryDetail(id: number): Promise<Category> {
  return request.get(`/training/categories/${id}`)
}

// 新增分类
export function createCategory(data: CategoryCreateParams): Promise<void> {
  return request.post('/training/categories', data)
}

// 更新分类
export function updateCategory(id: number, data: CategoryUpdateParams): Promise<void> {
  return request.put(`/training/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id: number): Promise<void> {
  return request.delete(`/training/categories/${id}`)
}

// 更新分类状态
export function updateCategoryStatus(id: number, status: number): Promise<void> {
  return request.put(`/training/categories/${id}/status`, { status })
}
