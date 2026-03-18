import request from '@/utils/request'

/**
 * 签到管理API
 */

// 获取签到记录列表
export function getAttendanceList(params: any) {
  return request.get('/attendance/records', { params })
}

// 签到
export function signIn(data: any) {
  return request.post('/attendance/records/sign', null, { params: data })
}

// 补签审核
export function auditAttendance(id: number, data: any) {
  return request.post(`/attendance/records/${id}/audit`, null, { params: data })
}

// 获取签到统计
export function getAttendanceStats(userId: number) {
  return request.get(`/attendance/records/stats/${userId}`)
}
