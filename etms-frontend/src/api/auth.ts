import request from '@/utils/request'
import type {
  ApiResponse,
  LoginParams,
  LoginResult,
  UserInfo
} from './types'

// 登录
export function login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/login', data)
}

// 登出
export function logout(): Promise<ApiResponse<void>> {
  return request.post('/auth/logout')
}

// 获取用户信息
export function getUserInfo(): Promise<ApiResponse<UserInfo>> {
  return request.get('/auth/info')
}

// 获取验证码
export function getCaptcha(): Promise<ApiResponse<{ captchaKey: string; captchaImage: string }>> {
  return request.post('/auth/captcha')
}
