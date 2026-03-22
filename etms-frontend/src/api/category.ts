import request from '@/utils/request'
import type {
  ApiResponse,
  Category,
  CategoryCreateParams,
  CategoryUpdateParams
} from './types'

// 获取分类树形结构
export function getCategoryTree(): Promise<ApiResponse<Category[]>> {
  return request.get('/training/categories/tree')
}

// 获取分类列表
export function getCategoryList(parentId?: number): Promise<ApiResponse<Category[]>> {
  return request.get('/training/categories', parentId ? { parentId } : undefined)
}

// 获取分类详情
export function getCategoryDetail(id: number): Promise<ApiResponse<Category>> {
  return request.get(`/training/categories/${id}`)
}

// 新增分类
export function createCategory(data: CategoryCreateParams): Promise<ApiResponse<void>> {
  return request.post('/training/categories', data)
}

// 更新分类
export function updateCategory(id: number, data: CategoryUpdateParams): Promise<ApiResponse<void>> {
  return request.put(`/training/categories/${id}`, data)
}

// 删除分类
export function deleteCategory(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/training/categories/${id}`)
}

// 检查分类编码是否存在
export function checkCategoryCode(categoryCode: string, excludeId?: number): Promise<ApiResponse<boolean>> {
  return request.get('/training/categories/check-code', { categoryCode, excludeId })
}

// 获取分类下课程数量
export function getCategoryCourseCount(id: number): Promise<ApiResponse<number>> {
  return request.get(`/training/categories/${id}/course-count`)
}
