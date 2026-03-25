import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { UserInfo } from '@/api/types'
import { STORAGE_KEYS } from '@/constants/storage'

// 从localStorage获取持久化的用户信息
const getPersistedUserInfo = (): UserInfo | null => {
  const stored = localStorage.getItem(STORAGE_KEYS.USER_INFO)
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
  const token = ref<string>(localStorage.getItem(STORAGE_KEYS.TOKEN) || '')
  const userInfo = ref<UserInfo | null>(getPersistedUserInfo())

  // 登录
  const loginAction = async (username: string, password: string, captcha: string, captchaKey: string) => {
    try {
      const res = await login({ username, password, captcha, captchaKey })
      token.value = res.accessToken
      localStorage.setItem(STORAGE_KEYS.TOKEN, res.accessToken)
      
      // 登录成功后获取完整用户信息
      await getUserInfoAction()
      
      return res
    } catch (error) {
      // 登录失败时清除token，避免残留导致状态不一致
      token.value = ''
      userInfo.value = null
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
      throw error
    }
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res
    // 持久化用户信息
    localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(res))
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
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
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
