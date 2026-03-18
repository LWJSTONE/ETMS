import request from '@/utils/request'

interface LoginParams {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
}

interface LoginResult {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  realName: string
  avatar: string
  deptName: string
  roles: string[]
  permissions: string[]
}

interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 登录
export function login(data: LoginParams): Promise<ApiResponse<LoginResult>> {
  return request.post('/auth/login', data)
}

// 登出
export function logout(): Promise<ApiResponse<void>> {
  return request.post('/auth/logout')
}

// 获取用户信息
export function getUserInfo(): Promise<ApiResponse<any>> {
  return request.get('/auth/info')
}

// 获取验证码
export function getCaptcha(): Promise<ApiResponse<{ captchaKey: string; captchaImage: string }>> {
  return request.post('/auth/captcha')
}
