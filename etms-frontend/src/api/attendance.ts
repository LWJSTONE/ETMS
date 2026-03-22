import request from '@/utils/request'

/**
 * 签到管理API
 */

// 获取签到记录列表
export function getAttendanceList(params: any) {
  return request.get('/attendance/records', params)
}

// 签到/签退
export function signIn(data: { planId: number; signType: number; location?: string }) {
  return request.post('/attendance/records/sign', null, { params: data })
}

// 签退
export function signOut(data: { planId: number; signType: number; location?: string }) {
  // 签退时signType固定为2（签退类型）
  return request.post('/attendance/records/sign', null, { params: { ...data, signType: 2 } })
}

// 补签申请
export function applySupplementary(data: { planId: number; signType: number; signTime: string; reason: string }) {
  return request.post('/attendance/records/supplementary', null, { params: data })
}

// 撤销补签申请
export function cancelSupplementary(id: number) {
  return request.delete(`/attendance/records/supplementary/${id}`)
}

// 补签审核
export function auditAttendance(id: number, data: { auditStatus: number; auditRemark?: string }) {
  return request.post(`/attendance/records/${id}/audit`, null, { params: data })
}

// 获取签到统计
export function getAttendanceStats(userId: number) {
  return request.get(`/attendance/records/stats/${userId}`)
}
