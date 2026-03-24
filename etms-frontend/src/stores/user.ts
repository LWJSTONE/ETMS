import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { UserInfo } from '@/api/types'

// userInfo持久化相关常量
const USER_INFO_KEY = 'userInfo'

// 从localStorage获取持久化的用户信息
const getPersistedUserInfo = (): UserInfo | null => {
  const stored = localStorage.getItem(USER_INFO_KEY)
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch {
      return null
    }
  }
  return null
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(getPersistedUserInfo())

  // 登录
  const loginAction = async (username: string, password: string, captcha: string, captchaKey: string) => {
    try {
      const res = await login({ username, password, captcha, captchaKey })
      token.value = res.data.accessToken
      localStorage.setItem('token', res.data.accessToken)
      
      // 登录成功后获取完整用户信息
      await getUserInfoAction()
      
      return res
    } catch (error) {
      // 登录失败时清除token，避免残留导致状态不一致
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem(USER_INFO_KEY)
      throw error
    }
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    // 持久化用户信息
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(res.data))
    return res
  }

  // 登出
  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      // 即使后端登出失败，也要清除本地状态
      console.error('登出接口调用失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem(USER_INFO_KEY)
    }
  }

  return {
    token,
    userInfo,
    loginAction,
    getUserInfoAction,
    logoutAction
  }
})
