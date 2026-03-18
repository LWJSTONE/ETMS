import request from '@/utils/request'

/**
 * 培训计划API
 */

// 获取培训计划列表
export function getPlanList(params: any) {
  return request.get('/training/plans', { params })
}

// 获取培训计划详情
export function getPlanDetail(id: number) {
  return request.get(`/training/plans/${id}`)
}

// 新增培训计划
export function addPlan(data: any) {
  return request.post('/training/plans', data)
}

// 更新培训计划
export function updatePlan(id: number, data: any) {
  return request.put(`/training/plans/${id}`, data)
}

// 删除培训计划
export function deletePlan(id: number) {
  return request.delete(`/training/plans/${id}`)
}

// 发布培训计划
export function publishPlan(id: number) {
  return request.post(`/training/plans/${id}/publish`)
}

// 归档培训计划
export function archivePlan(id: number) {
  return request.post(`/training/plans/${id}/archive`)
}

/**
 * 学习进度API
 */

// 获取学习进度列表
export function getProgressList(params: any) {
  return request.get('/training/progress', { params })
}

// 更新学习进度
export function updateProgress(data: any) {
  return request.put('/training/progress', data)
}

// 获取我的学习进度
export function getMyProgress(params: any) {
  return request.get('/training/progress/my', { params })
}
