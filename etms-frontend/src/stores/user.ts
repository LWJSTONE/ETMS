import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { UserInfo } from '@/api/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 登录
  const loginAction = async (username: string, password: string, captcha?: string, captchaKey?: string) => {
    const res = await login({ username, password, captcha, captchaKey })
    token.value = res.data.accessToken
    localStorage.setItem('token', res.data.accessToken)
    
    // 登录成功后获取完整用户信息
    await getUserInfoAction()
    
    return res
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  // 登出
  const logoutAction = async () => {
    await logout()
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    loginAction,
    getUserInfoAction,
    logoutAction
  }
})
