import request from '@/utils/request'
import type {
  PageResult,
  PageParams,
  AttendanceRecord,
  AttendanceSignInParams,
  AttendanceSupplementaryParams,
  AttendanceAuditParams,
  AttendanceStats
} from './types'

/**
 * 签到管理API
 */

// 获取签到记录列表
export function getAttendanceList(params: PageParams): Promise<PageResult<AttendanceRecord>> {
  return request.get('/attendance/records', params)
}

// 签到/签退
export function signIn(data: AttendanceSignInParams): Promise<void> {
  return request.post('/attendance/records/sign', data)
}

// 补签申请
export function applySupplementary(data: AttendanceSupplementaryParams): Promise<void> {
  return request.post('/attendance/records/supplementary', data)
}

// 撤销补签申请
export function cancelSupplementary(id: number): Promise<void> {
  return request.delete(`/attendance/records/supplementary/${id}`)
}

// 补签审核
export function auditAttendance(id: number, data: AttendanceAuditParams): Promise<void> {
  return request.post(`/attendance/records/${id}/audit`, data)
}

// 获取个人签到统计
export function getMyAttendanceStats(): Promise<AttendanceStats> {
  return request.get('/attendance/records/stats/personal')
}

// 获取指定用户签到统计（管理员）
export function getUserAttendanceStats(userId: number): Promise<AttendanceStats> {
  return request.get(`/attendance/records/stats/${userId}`)
}

/** @deprecated 请使用 getMyAttendanceStats 或 getUserAttendanceStats */
export function getAttendanceStats(userId: number): Promise<AttendanceStats> {
  return request.get(`/attendance/records/stats/${userId}`)
}

// 获取我的补签申请列表
export function getMySupplementaryList(params: PageParams): Promise<PageResult<AttendanceRecord>> {
  return request.get('/attendance/apply/my', params)
}
