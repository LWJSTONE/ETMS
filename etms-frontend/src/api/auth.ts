import request from '@/utils/request'
import type {
  LoginParams,
  LoginResult,
  UserInfo
} from './types'

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

// 获取验证码
// 修复：支持传递signal参数以取消请求，避免并发请求问题
export function getCaptcha(signal?: AbortSignal): Promise<{ captchaKey: string; captchaImage: string }> {
  return request.post('/auth/captcha', undefined, signal ? { signal } : undefined)
}
