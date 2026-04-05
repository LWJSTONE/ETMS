import request from '@/utils/request'
import type {
  LoginParams,
  LoginResult,
  UserInfo
} from './types'

// 修复：重导出类型，方便其他模块使用
export type { LoginParams, LoginResult, UserInfo }

// 登录
export function login(data: LoginParams): Promise<LoginResult> {
  return request.post('/auth/login', data)
}

// 登出
export function logout(): Promise<void> {
  return request.post('/auth/logout')
}

// 获取用户信息
export function getUserInfo(): Promise<UserInfo> {
  return request.get('/auth/info')
}


