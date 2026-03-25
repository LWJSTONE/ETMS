import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  TrainingPlan,
  TrainingPlanCreateParams,
  TrainingPlanUpdateParams,
  LearningProgress,
  LearningProgressUpdateParams
} from './types'

/**
 * 培训计划API
 */

// 获取培训计划列表
export function getPlanList(params: PageParams): Promise<PageResult<TrainingPlan>> {
  return request.get('/training/plans', params)
}

// 获取培训计划详情
export function getPlanDetail(id: number): Promise<TrainingPlan> {
  return request.get(`/training/plans/${id}`)
}

// 新增培训计划
export function createPlan(data: TrainingPlanCreateParams): Promise<void> {
  return request.post('/training/plans', data)
}

// 更新培训计划
export function updatePlan(id: number, data: TrainingPlanUpdateParams): Promise<void> {
  return request.put(`/training/plans/${id}`, data)
}

// 删除培训计划
export function deletePlan(id: number): Promise<void> {
  return request.delete(`/training/plans/${id}`)
}

// 发布培训计划
export function publishPlan(id: number): Promise<void> {
  return request.post(`/training/plans/${id}/publish`)
}

// 归档培训计划
export function archivePlan(id: number): Promise<void> {
  return request.post(`/training/plans/${id}/archive`)
}

// 结束培训计划
export function endPlan(id: number): Promise<void> {
  return request.post(`/training/plans/${id}/end`)
}

/**
 * 学习进度API
 */

// 获取学习进度列表
export function getProgressList(params: PageParams): Promise<PageResult<LearningProgress>> {
  return request.get('/training/progress', params)
}

// 更新学习进度
export function updateProgress(data: LearningProgressUpdateParams): Promise<void> {
  return request.put('/training/progress', data)
}

// 获取我的学习进度
export function getMyProgress(params: PageParams): Promise<PageResult<LearningProgress>> {
  return request.get('/training/progress/my', params)
}

// 获取学习进度详情
export function getProgressDetail(id: number): Promise<LearningProgress> {
  return request.get(`/training/progress/${id}`)
}
